package com.nhom7.quiz.quizapp.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    // CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // cho phép FE vite mặc định
        cfg.setAllowedOrigins(List.of("http://localhost:5173"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC
                        .requestMatchers(
                                "/api/login",
                                "/api/register",
                                "/api/admin/login",
                                "/api/quiz/public/**",
                                "/api/quiz/detail/**",
                                "/api/quiz/join/**",
                                "/api/quiz-attempts/public/**",
                                "/api/image/quiz/**",
                                "/api/user/avatars/**",
                                "/api/upload/avatars/**",
                                "/ws/**", "/topic/**", "/queue/**",
                                "/api/leaderboard/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll()

                        // BẢO VỆ (USER/ADMIN)
                        .requestMatchers(
                                "/api/user/**",
                                "/api/quiz/user/**",
                                "/api/quiz-attempts/**",
                                "/api/quiz/create-quiz-with-image",
                                "/api/answer/**",
                                "/api/result/**")
                        .hasAnyRole("USER", "ADMIN")

                        // CHỈNH Ở ĐÂY: endpoint câu hỏi phải có token (USER/ADMIN)
                        .requestMatchers("/api/question/**").hasAnyRole("USER", "ADMIN")

                        // REVIEW của quiz: cần đăng nhập
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/review").hasAnyRole("USER", "ADMIN")

                        // ADMIN
                        .requestMatchers(
                                "/api/admin/dashboard/**",
                                "/api/admin/users/**",
                                "/api/admin/quizzes/**",
                                "/api/admin/reports/**",
                                "/api/admin/analytics/**",
                                "/api/admin/attempts/**",
                                "/api/categories/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated())
                // Sửa mã trả về:
                // - Thiếu/invalid token => 401 (trước đây bạn trả 403 nên gây hiểu nhầm)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            res.setContentType("application/json;charset=UTF-8");
                            String json = """
                                        {"error":"UNAUTHORIZED","message":"Bạn cần đăng nhập để truy cập endpoint này"}
                                    """;
                            res.getWriter().write(json);
                        })
                        // - Có token nhưng không đủ quyền => 403
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            res.setContentType("application/json;charset=UTF-8");
                            String json = """
                                        {"error":"FORBIDDEN","message":"Bạn không có quyền truy cập tài nguyên này"}
                                    """;
                            res.getWriter().write(json);
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // phục vụ ảnh tĩnh uploads/**
    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:uploads/");
        }
    }
}
