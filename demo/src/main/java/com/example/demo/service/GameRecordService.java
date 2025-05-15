package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.GameRecord;
import java.util.List;

/**
 * 游戏记录服务接口
 */
public interface GameRecordService extends IService<GameRecord> {
    // 基础的CRUD操作已由IService提供
    
    /**
     * 获取带有用户名的游戏记录
     * @param id 记录ID
     * @return 游戏记录（包含用户名）
     */
    GameRecord getRecordWithUsername(Long id);
    
    /**
     * 获取用户的所有游戏记录，并填充用户名
     * @param userId 用户ID
     * @return 游戏记录列表
     */
    List<GameRecord> getUserRecordsWithUsername(Long userId);
    
    /**
     * 获取所有游戏记录，并填充用户名
     * @return 游戏记录列表
     */
    List<GameRecord> getAllRecordsWithUsername();
}