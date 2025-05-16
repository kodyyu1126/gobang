package com.example.demo.service.impl;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.controller.GameController.MoveResponse;
import com.example.demo.service.GameMessageService;

@Service
public class GameMessageServiceImpl implements GameMessageService {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageServiceImpl.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendGameStateMessage(MoveResponse moveResponse) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.GAME_EXCHANGE, 
                RabbitMQConfig.GAME_ROUTING_KEY, 
                moveResponse
            );
        } catch (AmqpConnectException e) {
            logger.warn("RabbitMQ连接失败，无法发送游戏状态消息: {}", e.getMessage());
        } catch (AmqpException e) {
            logger.warn("发送游戏状态消息失败: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("发送游戏状态消息时发生未知错误: {}", e.getMessage());
        }
    }
    
    @Override
    public void sendInitialGameState(String[][] board, String currentPlayer) {
        MoveResponse initialState = new MoveResponse();
        initialState.setSuccess(true);
        initialState.setMessage("初始游戏状态");
        initialState.setBoard(board);
        initialState.setCurrentPlayer(currentPlayer);
        
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.GAME_EXCHANGE, 
                RabbitMQConfig.GAME_ROUTING_KEY, 
                initialState
            );
        } catch (AmqpConnectException e) {
            logger.warn("RabbitMQ连接失败，无法发送初始游戏状态: {}", e.getMessage());
        } catch (AmqpException e) {
            logger.warn("发送初始游戏状态失败: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("发送初始游戏状态时发生未知错误: {}", e.getMessage());
        }
    }
} 