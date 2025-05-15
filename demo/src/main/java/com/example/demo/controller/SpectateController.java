package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.controller.GameController.MoveResponse;
import com.example.demo.service.GameMessageReceiver;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class SpectateController {
    
    @Autowired
    private GameMessageReceiver gameMessageReceiver;
    
    // 渲染观战席页面
    @GetMapping("/spectate")
    public String showSpectatePage(Model model) {
        // 获取最新游戏状态
        MoveResponse latestState = gameMessageReceiver.getLatestGameState();
        
        if (latestState != null && latestState.getBoard() != null) {
            log.info("观战席获取到游戏状态，当前玩家: {}", latestState.getCurrentPlayer());
            model.addAttribute("board", latestState.getBoard());
            model.addAttribute("currentPlayer", latestState.getCurrentPlayer());
        } else {
            // 如果还没有游戏状态，创建一个空棋盘
            log.warn("观战席无法获取游戏状态，使用空棋盘");
            model.addAttribute("board", new String[15][15]);
            model.addAttribute("currentPlayer", "black");
        }
        
        return "spectate";
    }
    
    // 获取最新游戏状态的API
    @GetMapping("/api/game-state")
    @ResponseBody
    public MoveResponse getGameState() {
        MoveResponse state = gameMessageReceiver.getLatestGameState();
        
        if (state == null) {
            // 如果没有状态，返回一个空状态
            MoveResponse emptyState = new MoveResponse();
            emptyState.setSuccess(false);
            emptyState.setMessage("等待游戏开始...");
            emptyState.setBoard(new String[15][15]);
            emptyState.setCurrentPlayer("black");
            return emptyState;
        }
        
        return state;
    }
} 