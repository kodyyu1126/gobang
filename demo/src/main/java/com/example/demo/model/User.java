package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("role")
    private String role;  // "ADMIN" 或 "USER"
    
    @TableField("enabled")
    private Boolean enabled;
    
    @TableField("phone")
    private String phone;
    
    @TableField("create_time")
    private Date createTime;

    // 构造函数
    public User() {
        this.enabled = true;
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }
    
    public User(String username, String password, String role, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.enabled = true;
    }
} 