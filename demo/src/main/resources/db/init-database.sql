-- 如果数据库不存在，则创建数据库
CREATE DATABASE IF NOT EXISTS `gobang_db` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `gobang_db`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  `phone` VARCHAR(20) DEFAULT NULL,
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 清理旧表（如果需要）
DROP TABLE IF EXISTS `game_record`;

-- 创建新的游戏记录表
CREATE TABLE IF NOT EXISTS `game_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `board_state` TEXT NOT NULL,
  `current_player` VARCHAR(10) NOT NULL,
  `save_time` BIGINT NOT NULL,
  `user_id` BIGINT,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

-- 插入默认管理员账号(admin/123456)
INSERT INTO `user` (`username`, `password`, `role`, `enabled`)
SELECT 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'ADMIN', TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

-- 插入默认用户账号(user/123456)
INSERT INTO `user` (`username`, `password`, `role`, `enabled`)
SELECT 'user', 'e10adc3949ba59abbe56e057f20f883e', 'USER', TRUE
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'user'); 