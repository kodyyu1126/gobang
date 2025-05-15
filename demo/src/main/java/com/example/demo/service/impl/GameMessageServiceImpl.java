package com.example.demo.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.controller.GameController.MoveResponse;
import com.example.demo.service.GameMessageService;

@Service
public class GameMessageServiceImpl implements GameMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendGameStateMessage(MoveResponse moveResponse) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.GAME_EXCHANGE, 
            RabbitMQConfig.GAME_ROUTING_KEY, 
            moveResponse
        );
    }
    
    @Override
    public void sendInitialGameState(String[][] board, String currentPlayer) {
        MoveResponse initialState = new MoveResponse();
        initialState.setSuccess(true);
        initialState.setMessage("初始游戏状态");
        initialState.setBoard(board);
        initialState.setCurrentPlayer(currentPlayer);
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.GAME_EXCHANGE, 
            RabbitMQConfig.GAME_ROUTING_KEY, 
            initialState
        );
    }
} 