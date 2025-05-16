package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes) {
        
        log.info("接收到注册请求，用户名：{}，手机号：{}", username, phone);
        
        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            redirectAttributes.addFlashAttribute("error", "手机号格式不正确");
            return "redirect:/register";
        }
        
        // 验证密码长度
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "密码长度不能少于6位");
            return "redirect:/register";
        }
        
        // 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        
        // 注册用户
        boolean success = userService.registerUser(user);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
            log.info("用户注册成功：{}", username);
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "注册失败，用户名或手机号已被使用");
            log.warn("用户注册失败：{}", username);
            return "redirect:/register";
        }
    }
} 