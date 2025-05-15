package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.util.MD5PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("配置安全过滤链");
        
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/register", "/spectate/**", "/CSS/**", "/JS/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/startgame", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .permitAll()
            )
            // 对游戏API端点禁用CSRF保护，确保AJAX请求能正常工作
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/move", 
                    "/reset", 
                    "/toggle-ai", 
                    "/ai-move", 
                    "/game/save", 
                    "/game/load/**", 
                    "/game/delete/**", 
                    "/game/records",
                    "/reset-state",
                    "/register"
                )
            );
        
        log.info("安全过滤链配置完成");
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MD5PasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
} 