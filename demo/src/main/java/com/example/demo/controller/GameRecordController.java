package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.model.GameRecord;
import com.example.demo.model.User;
import com.example.demo.service.GameRecordService;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

// 游戏记录控制器
@RestController
@RequestMapping("/game")
@Slf4j
public class GameRecordController {
    
    @Autowired
    private GameRecordService gameRecordService;
    
    @Autowired
    private UserService userService;
    
    // 保存游戏记录
    @PostMapping("/save")
    public ResponseEntity<?> saveGame(@RequestBody GameRecord gameRecord) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            log.info("接收到保存请求，用户名: {}", username);
            
            User user = userService.getByUsername(username);
            if (user == null) {
                log.error("保存记录失败：找不到用户 {}", username);
                return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("找不到用户");
            }
            
            if (gameRecord.getSaveTime() == null) {
                gameRecord.setSaveTime(System.currentTimeMillis());
            }
            
            if (gameRecord.getBoardState() == null) {
                log.error("保存记录失败：棋盘状态为空");
                return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("棋盘状态不能为空");
            }
            
            // 设置用户ID（暂不设置username，因为数据库可能不包含此列）
            gameRecord.setUserId(user.getId());
            
            // 使用MyBatis-Plus的save方法
            boolean success = gameRecordService.save(gameRecord);
            
            if (success) {
                log.info("记录保存成功，ID: {}", gameRecord.getId());
                return ResponseEntity.ok(gameRecord);
            } else {
                log.error("记录保存失败");
                return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("保存失败");
            }
        } catch (Exception e) {
            log.error("保存记录时发生错误", e);
            return ResponseEntity.status(500)
                .contentType(MediaType.TEXT_PLAIN)
                .body("保存失败: " + e.getMessage());
        }
    }
    
    // 获取当前用户的游戏记录列表
    @GetMapping("/records")
    public ResponseEntity<?> getUserRecords() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            log.info("获取用户 {} 的记录列表", username);
            
            // 获取用户ID
            User user = userService.getByUsername(username);
            if (user == null) {
                log.error("获取记录失败：找不到用户 {}", username);
                return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("找不到用户");
            }
            
            // 如果是管理员，可以查看所有记录
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                    
            List<GameRecord> records;
            if (isAdmin) {
                records = gameRecordService.getAllRecordsWithUsername();
                log.info("管理员查看所有记录，总数: {}", records.size());
            } else {
                // 普通用户只能查看自己的记录
                records = gameRecordService.getUserRecordsWithUsername(user.getId());
                log.info("用户 {} 的记录数: {}", username, records.size());
            }
            
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("获取记录列表时发生错误", e);
            return ResponseEntity.status(500)
                .contentType(MediaType.TEXT_PLAIN)
                .body("获取记录失败: " + e.getMessage());
        }
    }
    
    // 加载游戏记录
    @GetMapping("/load/{id}")
    public ResponseEntity<?> loadGame(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            // 获取用户ID
            User user = userService.getByUsername(username);
            if (user == null) {
                log.error("加载记录失败：找不到用户 {}", username);
                return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("找不到用户");
            }
            
            log.info("用户 {} 请求加载记录 ID: {}", username, id);
            
            // 获取记录并填充用户名
            GameRecord record = gameRecordService.getRecordWithUsername(id);
            
            // 检查记录是否存在
            if (record == null) {
                log.warn("记录 ID: {} 不存在", id);
                return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("记录不存在");
            }
            
            // 权限检查：只有管理员或记录所有者可以加载记录
            if (isAdmin || record.getUserId().equals(user.getId())) {
                log.info("加载记录成功，记录 ID: {}", id);
                return ResponseEntity.ok(record);
            } else {
                log.warn("用户 {} 无权限加载记录 ID: {}", username, id);
                return ResponseEntity.status(403)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("没有权限加载此记录");
            }
        } catch (Exception e) {
            log.error("加载记录时发生错误", e);
            return ResponseEntity.status(500)
                .contentType(MediaType.TEXT_PLAIN)
                .body("加载记录失败: " + e.getMessage());
        }
    }
    
    // 删除游戏记录
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            // 获取用户ID
            User user = userService.getByUsername(username);
            if (user == null) {
                log.error("删除记录失败：找不到用户 {}", username);
                return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("找不到用户");
            }
            
            log.info("用户 {} 请求删除记录 ID: {}", username, id);
            
            // 获取记录
            GameRecord record = gameRecordService.getById(id);
            
            // 检查记录是否存在
            if (record == null) {
                log.warn("记录 ID: {} 不存在", id);
                return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("记录不存在");
            }
            
            // 权限检查：只有管理员或记录所有者可以删除记录
            if (isAdmin || record.getUserId().equals(user.getId())) {
                boolean success = gameRecordService.removeById(id);
                if (success) {
                    log.info("记录 ID: {} 删除成功", id);
                    return ResponseEntity.ok().build();
                } else {
                    log.error("记录 ID: {} 删除失败", id);
                    return ResponseEntity.status(500)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("删除失败");
                }
            } else {
                log.warn("用户 {} 无权限删除记录 ID: {}", username, id);
                return ResponseEntity.status(403)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("没有权限删除此记录");
            }
        } catch (Exception e) {
            log.error("删除记录时发生错误", e);
            return ResponseEntity.status(500)
                .contentType(MediaType.TEXT_PLAIN)
                .body("删除记录失败: " + e.getMessage());
        }
    }
}