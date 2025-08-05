package com.nhom7.quiz.quizapp.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Cho phép tất cả origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("🔐 Configuring Security Filter Chain with Role-Based Access");

        http
                .cors().and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(auth -> auth
                        // ✅ PUBLIC ENDPOINTS - Không cần authentication
                        .requestMatchers(
                                "/api/login", 
                                "/api/register", 
                                "/api/image/quiz/*", 
                                "/api/categories", // ✅ Chỉ GET categories là public
                                "/api/user/avatars/**", 
                                "/api/upload/avatars/**", 
                                "/api/quiz/join/*",
                                "/api/quiz/public/**", 
                                "/api/image/quiz**", 
                                "/api/quiz/detail/**", 
                                "/api/question/**",
                                "/api/quiz-attempts/public/recent/**",
                                "/api/quizzes/**"
                        ).permitAll()
                        
                        // ✅ ADMIN-ONLY ENDPOINTS
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/admin/dashboard/**",
                                "/api/admin/users/**",
                                "/api/admin/quizzes/**",
                                "/api/admin/reports/**",
                                "/api/admin/analytics/**",
                                "/api/admin/attempts/**"
                        ).hasRole("ADMIN")
                        
                        // ✅ USER ENDPOINTS - Cần authentication
                        .requestMatchers(
                                "/api/user/**",
                                "/api/quiz/**",
                                "/api/quiz-attempts/**"
                        ).hasAnyRole("USER", "ADMIN")
                        
                        // ✅ CATEGORIES CRUD - Chỉ admin mới được tạo/sửa/xóa
                        .requestMatchers(
                                "/api/categories/**"
                        ).hasRole("ADMIN")
                        
                        // ✅ DEFAULT - Tất cả request khác cần authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration
    public static class WebConfig implements WebMvcConfigurer {

        @Override
        public void addCorsMappings(@NonNull CorsRegistry registry) {
            registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);
        }

        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:uploads/");
        }
    }
}
