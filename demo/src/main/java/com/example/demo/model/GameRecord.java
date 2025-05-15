package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 游戏记录实体类
 */
@Data
@TableName("game_record")
public class GameRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("board_state")
    private String boardState;
    
    @TableField("current_player")
    private String currentPlayer;
    
    @TableField("save_time")
    private Long saveTime;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField(exist = false)
    private String username;

    // 构造函数
    public GameRecord() {}

    public GameRecord(String boardState, String currentPlayer, Long saveTime) {
        this.boardState = boardState;
        this.currentPlayer = currentPlayer;
        this.saveTime = saveTime;
    }
    
    public GameRecord(String boardState, String currentPlayer, Long saveTime, Long userId, String username) {
        this.boardState = boardState;
        this.currentPlayer = currentPlayer;
        this.saveTime = saveTime;
        this.userId = userId;
        this.username = username;
    }
}