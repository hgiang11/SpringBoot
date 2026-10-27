package com.thg.chickenshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Công khai: trang giới thiệu, menu, đăng nhập/đăng ký, file tĩnh, trang lỗi
                        .requestMatchers("/", "/home", "/menu", "/jollibee",
                                "/login", "/register", "/error",
                                "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        // Chỉ ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Còn lại (giỏ hàng, thanh toán, đơn hàng...) phải đăng nhập
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/menu", false)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}