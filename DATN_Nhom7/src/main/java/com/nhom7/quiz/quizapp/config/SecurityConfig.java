package com.nhom7.quiz.quizapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    // Cấu hình bảo mật sẽ được thêm vào đây trong tương lai

    // Mã hóa mật khẩu sử dụng BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // Cấu hình người dùng trong bộ nhớ (In-Memory)
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User
            .withUsername("admin")
            .password(passwordEncoder().encode("admin")) 
            .roles("USER") 
            .build();

        return new InMemoryUserDetailsManager(admin);
    }
    // Cấu hình chuỗi bảo mật
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .httpBasic(); 

        return http.build();
    }
}
