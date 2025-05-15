package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // 交换机名称
    public static final String GAME_EXCHANGE = "game.exchange";
    // 路由键
    public static final String GAME_ROUTING_KEY = "game.move";
    // 队列名称
    public static final String GAME_QUEUE = "game.queue";
    
    // 声明Direct交换机
    @Bean
    public DirectExchange gameExchange() {
        return new DirectExchange(GAME_EXCHANGE);
    }
    
    // 声明队列
    @Bean
    public Queue gameQueue() {
        return new Queue(GAME_QUEUE);
    }
    
    // 将队列绑定到交换机
    @Bean
    public Binding gameBinding(Queue gameQueue, DirectExchange gameExchange) {
        return BindingBuilder.bind(gameQueue).to(gameExchange).with(GAME_ROUTING_KEY);
    }
    
    // 配置消息转换器 - 将Java对象转换为JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    // 配置RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
} 