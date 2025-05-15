package com.example.demo.service;

import com.example.demo.controller.GameController.MoveResponse;

public interface GameMessageService {
    // 发送游戏状态消息
    void sendGameStateMessage(MoveResponse moveResponse);

    // 发送初始游戏状态
    void sendInitialGameState(String[][] board, String currentPlayer);
} 