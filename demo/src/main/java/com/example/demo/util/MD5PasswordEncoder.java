package com.example.demo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * MD5密码编码器 - 用于Spring Security的密码加密
 * 
 * 注意: 此类是项目中处理密码加密的首选工具类，已在SecurityConfig中配置为默认的密码编码器。
 * 在生产环境中，应考虑使用更安全的加密算法，如bcrypt或PBKDF2。
 */
public class MD5PasswordEncoder implements PasswordEncoder {

    /**
     * 使用MD5加密密码
     */
    @Override
    public String encode(CharSequence rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            
            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 验证密码是否匹配
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
} 