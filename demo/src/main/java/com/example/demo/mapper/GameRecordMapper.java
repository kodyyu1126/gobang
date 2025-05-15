package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.GameRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏记录Mapper接口
 */
@Mapper
public interface GameRecordMapper extends BaseMapper<GameRecord> {
    // 基础的CRUD操作已由BaseMapper提供
    // 可以在这里添加自定义查询方法
}