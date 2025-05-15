package com.example.demo.service;

/**
 * 游戏状态服务接口
 * 用于管理用户的游戏状态
 */
public interface GameStateService {
    
    /**
     * 初始化用户的游戏状态
     * @param username 用户名
     */
    void initializeGameState(String username);
    
    /**
     * 获取用户的棋盘状态
     * @param username 用户名
     * @return 棋盘状态
     */
    String[][] getBoard(String username);
    
    /**
     * 设置用户的棋盘状态
     * @param username 用户名
     * @param board 棋盘状态
     * @return 设置的棋盘状态
     */
    String[][] setBoard(String username, String[][] board);
    
    /**
     * 获取用户的当前玩家
     * @param username 用户名
     * @return 当前玩家
     */
    String getCurrentPlayer(String username);
    
    /**
     * 设置用户的当前玩家
     * @param username 用户名
     * @param currentPlayer 当前玩家
     * @return 设置的当前玩家
     */
    String setCurrentPlayer(String username, String currentPlayer);
    
    /**
     * 获取用户的AI模式状态
     * @param username 用户名
     * @return AI模式状态
     */
    boolean getAiMode(String username);
    
    /**
     * 设置用户的AI模式状态
     * @param username 用户名
     * @param aiMode AI模式状态
     * @return 设置的AI模式状态
     */
    boolean setAiMode(String username, boolean aiMode);
    
    /**
     * 清除用户的游戏状态
     * @param username 用户名
     */
    void clearGameState(String username);
} 