package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.GameRecordMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.GameRecord;
import com.example.demo.model.User;
import com.example.demo.service.GameRecordService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏记录服务实现类
 */
@Service
public class GameRecordServiceImpl extends ServiceImpl<GameRecordMapper, GameRecord> implements GameRecordService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public GameRecord getRecordWithUsername(Long id) {
        GameRecord record = this.getById(id);
        if (record != null && record.getUserId() != null) {
            User user = userService.getById(record.getUserId());
            if (user != null) {
                record.setUsername(user.getUsername());
            }
        }
        return record;
    }

    @Override
    public List<GameRecord> getUserRecordsWithUsername(Long userId) {
        LambdaQueryWrapper<GameRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GameRecord::getUserId, userId);
        List<GameRecord> records = this.list(queryWrapper);
        
        // 获取用户信息
        if (!records.isEmpty()) {
            User user = userService.getById(userId);
            if (user != null) {
                String username = user.getUsername();
                // 为所有记录设置用户名
                records.forEach(record -> record.setUsername(username));
            }
        }
        
        return records;
    }

    @Override
    public List<GameRecord> getAllRecordsWithUsername() {
        List<GameRecord> records = this.list();
        if (records.isEmpty()) {
            return records;
        }
        
        // 收集所有不同的用户ID
        List<Long> userIds = records.stream()
                .map(GameRecord::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (!userIds.isEmpty()) {
            // 批量查询用户信息
            List<User> users = userService.listByIds(userIds);
            Map<Long, String> userIdToNameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
            
            // 为每条记录设置用户名
            records.forEach(record -> {
                if (record.getUserId() != null) {
                    record.setUsername(userIdToNameMap.get(record.getUserId()));
                }
            });
        }
        
        return records;
    }
}