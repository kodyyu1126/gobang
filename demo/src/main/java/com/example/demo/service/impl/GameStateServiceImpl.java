package com.example.demo.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.demo.service.GameStateService;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GameStateServiceImpl implements GameStateService {

    // 缓存名称
    private static final String BOARD_CACHE = "gameBoard";
    private static final String PLAYER_CACHE = "currentPlayer";
    private static final String AI_MODE_CACHE = "aiMode";
    
    /**
     * 初始化用户的游戏状态
     */
    @Override
    public void initializeGameState(String username) {
        log.info("初始化用户 {} 的游戏状态", username);
        // 创建一个新的空棋盘
        String[][] emptyBoard = createEmptyBoard();
        setBoard(username, emptyBoard);
        setCurrentPlayer(username, "black");
        setAiMode(username, false);
    }
    
    /**
     * 创建空棋盘
     */
    private String[][] createEmptyBoard() {
        String[][] board = new String[15][15];
        // 确保每个元素都是null
        for (int i = 0; i < 15; i++) {
            Arrays.fill(board[i], null);
        }
        return board;
    }
    
    /**
     * 深复制棋盘
     */
    private String[][] deepCopyBoard(String[][] board) {
        if (board == null) {
            return createEmptyBoard();
        }
        
        String[][] copy = new String[board.length][];
        for (int i = 0; i < board.length; i++) {
            copy[i] = Arrays.copyOf(board[i], board[i].length);
        }
        return copy;
    }
    
    /**
     * 获取用户的棋盘状态
     */
    @Override
    @Cacheable(value = BOARD_CACHE, key = "#username")
    public String[][] getBoard(String username) {
        log.debug("获取用户 {} 的棋盘状态(缓存未命中)", username);
        // 如果缓存未命中，返回一个新的空棋盘
        return createEmptyBoard();
    }
    
    /**
     * 设置用户的棋盘状态
     */
    @Override
    @CachePut(value = BOARD_CACHE, key = "#username")
    public String[][] setBoard(String username, String[][] board) {
        log.debug("设置用户 {} 的棋盘状态", username);
        // 深复制棋盘，避免引用问题
        return deepCopyBoard(board);
    }
    
    /**
     * 获取用户的当前玩家
     */
    @Override
    @Cacheable(value = PLAYER_CACHE, key = "#username")
    public String getCurrentPlayer(String username) {
        log.debug("获取用户 {} 的当前玩家(缓存未命中)", username);
        // 如果缓存未命中，返回默认值
        return "black";
    }
    
    /**
     * 设置用户的当前玩家
     */
    @Override
    @CachePut(value = PLAYER_CACHE, key = "#username")
    public String setCurrentPlayer(String username, String currentPlayer) {
        log.debug("设置用户 {} 的当前玩家为 {}", username, currentPlayer);
        return currentPlayer;
    }
    
    /**
     * 获取用户的AI模式状态
     */
    @Override
    @Cacheable(value = AI_MODE_CACHE, key = "#username")
    public boolean getAiMode(String username) {
        log.debug("获取用户 {} 的AI模式状态(缓存未命中)", username);
        // 如果缓存未命中，返回默认值
        return false;
    }
    
    /**
     * 设置用户的AI模式状态
     */
    @Override
    @CachePut(value = AI_MODE_CACHE, key = "#username")
    public boolean setAiMode(String username, boolean aiMode) {
        log.debug("设置用户 {} 的AI模式状态为 {}", username, aiMode);
        return aiMode;
    }
    
    /**
     * 清除用户的游戏状态
     */
    @Override
    @CacheEvict(value = {BOARD_CACHE, PLAYER_CACHE, AI_MODE_CACHE}, key = "#username")
    public void clearGameState(String username) {
        log.info("清除用户 {} 的游戏状态", username);
    }
} 