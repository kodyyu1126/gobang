package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    // 基础的CRUD操作已由IService提供
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getByUsername(String username);
    
    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户对象
     */
    User getByPhone(String phone);
    
    /**
     * 创建新用户
     * @param user 用户信息
     * @return 是否成功
     */
    boolean registerUser(User user);
} 