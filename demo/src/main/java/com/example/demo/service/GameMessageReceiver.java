package com.example.demo.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.controller.GameController.MoveResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GameMessageReceiver {

    // 存储最新游戏状态
    private volatile MoveResponse latestGameState;

    @RabbitListener(queues = RabbitMQConfig.GAME_QUEUE)
    public void receiveGameState(MoveResponse gameState) {
        
        log.info("接收到游戏状态消息: {}", gameState.getMessage());
        if (gameState.getBoard() == null) {
            log.error("接收到的游戏状态棋盘为空!");
        } else {
            log.info("棋盘状态正常，当前玩家: {}", gameState.getCurrentPlayer());
        }
        
        this.latestGameState = gameState;
    }
    
    // 获取最新游戏状态
    public MoveResponse getLatestGameState() {
        if (latestGameState == null) {
            log.warn("当前没有游戏状态数据");
        }
        return latestGameState;
    }
} 