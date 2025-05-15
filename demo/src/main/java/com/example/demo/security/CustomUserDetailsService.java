package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import java.util.Collections;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;
    
    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.info("尝试认证用户: {}", login);
        
        User user = null;
        
        // 判断输入是否为手机号
        if (PHONE_PATTERN.matcher(login).matches()) {
            log.info("使用手机号登录: {}", login);
            user = userService.getByPhone(login);
        } else {
            log.info("使用用户名登录: {}", login);
            user = userService.getByUsername(login);
        }
        
        // 如果用户不存在
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + login);
        }
        
        log.info("用户认证成功: {}, 角色: {}", user.getUsername(), user.getRole());
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getEnabled(),
            true, true, true,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
} 