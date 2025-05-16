package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.User;
import com.example.demo.service.GameRecordService;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private GameRecordService gameRecordService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("gameRecords", gameRecordService.getAllRecordsWithUsername());
        model.addAttribute("users", userService.list());
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String showUsers(Model model) {
        model.addAttribute("users", userService.list());
        model.addAttribute("newUser", new User());
        return "admin/users";
    }
    
    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            log.info("添加新用户: {}", user.getUsername());
            
            // 验证用户名是否已存在
            if (userService.getByUsername(user.getUsername()) != null) {
                redirectAttributes.addFlashAttribute("error", "用户名已存在");
                return "redirect:/admin/users";
            }
            
            // 验证手机号是否已存在(如果提供了手机号)
            if (user.getPhone() != null && !user.getPhone().isEmpty() && 
                userService.getByPhone(user.getPhone()) != null) {
                redirectAttributes.addFlashAttribute("error", "手机号已被使用");
                return "redirect:/admin/users";
            }
            
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 确保默认值
            if (user.getEnabled() == null) {
                user.setEnabled(true);
            }
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("USER");
            }
            
            // 保存用户
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("success", "用户添加成功");
            log.info("用户添加成功: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("添加用户失败: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "添加用户失败: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/toggle")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
            
            // 不允许禁用管理员账号
            if ("ADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "不能修改管理员账号状态");
                return "redirect:/admin/users";
            }
            
            // 切换状态
            user.setEnabled(!user.getEnabled());
            userService.updateById(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "用户 " + user.getUsername() + " 已" + (user.getEnabled() ? "启用" : "禁用"));
            log.info("用户状态已更改: {} -> {}", user.getUsername(), user.getEnabled());
            
        } catch (Exception e) {
            log.error("切换用户状态失败: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "用户不存在");
                return "redirect:/admin/users";
            }
            
            // 不允许删除管理员账号
            if ("ADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "不能删除管理员账号");
                return "redirect:/admin/users";
            }
            
            userService.removeById(id);
            
            redirectAttributes.addFlashAttribute("success", "用户删除成功");
            log.info("用户已删除: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
} 