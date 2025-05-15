package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有管理员账户
        if (userService.getByUsername("admin") == null) {
            User adminUser = new User(
                "admin",
                passwordEncoder.encode("123456"),
                "ADMIN"
            );
            userService.save(adminUser);
            log.info("创建默认管理员账户: admin");
        }
        
        // 检查是否已有普通用户账户
        if (userService.getByUsername("user") == null) {
            User normalUser = new User(
                "user",
                passwordEncoder.encode("123456"),
                "USER"
            );
            userService.save(normalUser);
            log.info("创建默认普通用户账户: user");
        }
    }
} 