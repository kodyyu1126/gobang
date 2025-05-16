package com.example.demo.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.service.GameMessageService;
import com.example.demo.service.GameStateService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class GameController {
    
    @Autowired
    private GameMessageService gameMessageService;
    
    @Autowired
    private GameStateService gameStateService;
    
    // 获取当前登录用户名
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "anonymousUser";
    }
    
    // 渲染五子棋页面
    @GetMapping("/startgame")
    public String showGamePage(Model model) {
        String username = getCurrentUsername();
        log.info("用户 {} 访问游戏页面", username);
        
        // 检查是否需要初始化
        try {
            // 获取棋盘和当前玩家，如果缓存中不存在，会自动初始化
            String[][] board = gameStateService.getBoard(username);
            String currentPlayer = gameStateService.getCurrentPlayer(username);
            
            model.addAttribute("board", board);
            model.addAttribute("currentPlayer", currentPlayer);
            
            // 发送初始游戏状态到RabbitMQ
            try {
                gameMessageService.sendInitialGameState(board, currentPlayer);
                log.info("初始游戏状态已发送到观战席");
            } catch (Exception e) {
                log.error("发送初始游戏状态失败: {}", e.getMessage());
            }
        } catch (Exception e) {
            // 如果获取失败，初始化新的游戏状态
            log.warn("获取游戏状态失败，初始化新的游戏状态: {}", e.getMessage());
            gameStateService.initializeGameState(username);
            
            // 重新获取并设置模型属性
            model.addAttribute("board", gameStateService.getBoard(username));
            model.addAttribute("currentPlayer", gameStateService.getCurrentPlayer(username));
        }
        
        return "wuziqi";
    }

    // 重置棋盘接口
    @GetMapping("/reset")
    @ResponseBody
    public String resetBoard() {
        String username = getCurrentUsername();
        try {
            // 清除用户的旧游戏状态
            gameStateService.clearGameState(username);
            
            // 重新初始化游戏状态
            gameStateService.initializeGameState(username);
            
            String[][] board = gameStateService.getBoard(username);
            String currentPlayer = gameStateService.getCurrentPlayer(username);
            
            log.info("用户 {} 重置了棋盘，当前玩家：{}", username, currentPlayer);
            
            // 发送重置后的游戏状态
            try {
                gameMessageService.sendInitialGameState(board, currentPlayer);
                log.info("重置后的游戏状态已发送到观战席");
            } catch (Exception e) {
                log.error("发送重置状态失败: {}", e.getMessage());
            }
            
            return "棋盘已重置";
        } catch (Exception e) {
            log.error("重置棋盘失败: {}", e.getMessage());
            return "重置棋盘失败: " + e.getMessage();
        }
    }

    // 处理落子请求
    @PostMapping("/move")
    @ResponseBody
    public MoveResponse makeMove(@RequestParam int x, @RequestParam int y, @RequestParam String player) {
        String username = getCurrentUsername();
        MoveResponse response = new MoveResponse();
        
        try {
            log.info("用户 {} 发送落子请求: x={}, y={}, player={}", username, x, y, player);
            
            // 获取当前棋盘状态
            String[][] board = gameStateService.getBoard(username);
            String currentPlayer = gameStateService.getCurrentPlayer(username);
            
            // 详细记录棋盘当前状态
            log.info("当前棋盘状态: {}", Arrays.deepToString(board));
            log.info("当前玩家: {}", currentPlayer);
            
            // 检验合法性：检查移动是否有效
            if (x < 0 || x >= 15 || y < 0 || y >= 15) {
                response.setSuccess(false);
                response.setMessage("坐标无效");
                log.warn("落子失败：坐标无效 x={}, y={}", x, y);
                return response;
            }
            
            if (board[y][x] != null) {
                response.setSuccess(false);
                response.setMessage("该位置已有棋子");
                log.warn("落子失败：位置({},{})已有棋子", x, y);
                return response;
            }
            
            if (!player.equals(currentPlayer)) {
                response.setSuccess(false);
                response.setMessage("不是您的回合");
                log.warn("落子失败：非当前玩家回合, 期望={}, 实际={}", currentPlayer, player);
                return response;
            }

            log.info("落子验证通过，执行落子: ({},{}), 玩家={}", x, y, player);
            
            // 创建棋盘的副本，避免直接修改缓存中的引用
            String[][] newBoard = Arrays.stream(board)
                .map(row -> Arrays.copyOf(row, row.length))
                .toArray(String[][]::new);
            
            // 位置合法 -> 执行移动
            newBoard[y][x] = player;
            String nextPlayer = player.equals("black") ? "white" : "black";
            
            // 更新缓存
            gameStateService.setBoard(username, newBoard);
            gameStateService.setCurrentPlayer(username, nextPlayer);
            
            // 设置响应
            response.setSuccess(true);
            response.setMessage("落子成功");
            response.setBoard(newBoard);
            response.setCurrentPlayer(nextPlayer);
            
            log.info("落子成功，下一回合玩家: {}", nextPlayer);
            
            // 发送游戏状态到RabbitMQ
            try {
                gameMessageService.sendGameStateMessage(response);
                log.info("新的落子 ({},{}) 已发送到观战席", x, y);
            } catch (Exception e) {
                log.error("发送游戏状态失败: {}", e.getMessage());
                // 不影响主要流程，继续执行
            }
        
            return response;
        } catch (Exception e) {
            log.error("处理落子请求失败: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("服务器错误: " + e.getMessage());
            return response;
        }
    }

    // AI部分
    // 获取AI移动
    @GetMapping("/ai-move")
    @ResponseBody
    public MoveResponse getAiMove() {
        String username = getCurrentUsername();
        MoveResponse response = new MoveResponse();
        
        try {
            log.info("用户 {} 请求AI寻找落子位置", username);
            
            String[][] board = gameStateService.getBoard(username);
            
            // 简单AI逻辑：找到第一个空位
            for (int y = 0; y < 15; y++) {
                for (int x = 0; x < 15; x++) {
                    if (board[y][x] == null) {
                        int[] aiMove = {x, y};
                        response.setSuccess(true);
                        response.setMessage("AI找到了一个移动");
                        response.setAiMove(aiMove);
                        
                        log.info("AI选择落子位置: ({},{})", x, y);
                        return response;
                    }
                }
            }
            
            log.warn("AI未找到有效落子位置");
            response.setSuccess(false);
            response.setMessage("无法找到有效移动");
            return response;
        } catch (Exception e) {
            log.error("AI移动失败: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("服务器错误: " + e.getMessage());
            return response;
        }
    }

    // 切换AI模式
    @GetMapping("/toggle-ai")
    @ResponseBody
    public boolean toggleAi() {
        String username = getCurrentUsername();
        boolean aiMode = gameStateService.getAiMode(username);
        aiMode = !aiMode;
        gameStateService.setAiMode(username, aiMode);
        log.info("用户 {} 切换AI模式为: {}", username, aiMode);
        return aiMode;
    }
    
    // 重置游戏状态为加载的棋局
    @PostMapping("/reset-state")
    @ResponseBody
    public MoveResponse resetGameState(@RequestBody GameStateRequest request) {
        String username = getCurrentUsername();
        MoveResponse response = new MoveResponse();
        
        try {
            log.info("用户 {} 请求重置游戏状态为加载的棋局", username);
            
            if (request.getBoardState() == null || request.getCurrentPlayer() == null) {
                response.setSuccess(false);
                response.setMessage("请求参数不完整");
                log.warn("重置棋局状态失败：参数不完整");
                return response;
            }
            
            // 解析棋盘状态
            try {
                // 首先清除现有状态
                gameStateService.clearGameState(username);
                
                String[][] board;
                
                // 如果 boardState 是 JSON 字符串，需要解析
                if (request.getBoardState() instanceof String) {
                    // 使用 Jackson 解析
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    board = mapper.readValue((String)request.getBoardState(), String[][].class);
                } else if (request.getBoardState() instanceof String[][]) {
                    // 如果已经是字符串数组，做深复制
                    String[][] sourceBoard = (String[][])request.getBoardState();
                    board = Arrays.stream(sourceBoard)
                        .map(row -> Arrays.copyOf(row, row.length))
                        .toArray(String[][]::new);
                } else {
                    throw new IllegalArgumentException("不支持的棋盘状态类型");
                }
                
                String currentPlayer = request.getCurrentPlayer();
                
                // 更新缓存
                gameStateService.setBoard(username, board);
                gameStateService.setCurrentPlayer(username, currentPlayer);
                
                // 设置AI模式为false
                gameStateService.setAiMode(username, false);
                
                // 获取更新后的状态（这会从缓存中获取）
                String[][] updatedBoard = gameStateService.getBoard(username);
                String updatedPlayer = gameStateService.getCurrentPlayer(username);
                
                // 发送游戏状态到观战席
                gameMessageService.sendInitialGameState(updatedBoard, updatedPlayer);
                
                response.setSuccess(true);
                response.setMessage("游戏状态已重置");
                response.setBoard(updatedBoard);
                response.setCurrentPlayer(updatedPlayer);
                
                log.info("游戏状态重置成功，当前玩家: {}", updatedPlayer);
                return response;
                
            } catch (Exception e) {
                log.error("解析棋盘状态失败: {}", e.getMessage(), e);
                response.setSuccess(false);
                response.setMessage("解析棋盘状态失败: " + e.getMessage());
                return response;
            }
            
        } catch (Exception e) {
            log.error("重置游戏状态失败: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("服务器错误: " + e.getMessage());
            return response;
        }
    }

    // 移动响应类
    public static class MoveResponse implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private boolean success;
        private String message;
        private String[][] board;
        private String currentPlayer;
        private int[] aiMove;

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String[][] getBoard() { return board; }
        public void setBoard(String[][] board) { this.board = board; }
        public String getCurrentPlayer() { return currentPlayer; }
        public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
        public int[] getAiMove() { return aiMove; }
        public void setAiMove(int[] aiMove) { this.aiMove = aiMove; }
    }

    // 游戏状态请求类
    public static class GameStateRequest implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Object boardState;  // 可以是 String 或 String[][]
        private String currentPlayer;

        // getters and setters
        public Object getBoardState() { return boardState; }
        public void setBoardState(Object boardState) { this.boardState = boardState; }
        public String getCurrentPlayer() { return currentPlayer; }
        public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
    }
}
