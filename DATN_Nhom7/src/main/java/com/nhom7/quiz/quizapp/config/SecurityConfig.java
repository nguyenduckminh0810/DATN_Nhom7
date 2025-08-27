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
    @Autowired
    private BannedUserFilter bannedUserFilter;

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

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS - Không cần authentication
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll() // CHỈ GET categories
                        .requestMatchers(
                                "/api/login",
                                "/api/register",
                                "/api/auth/forgot-password-code",
                                "/api/auth/verify-reset-code",
                                "/api/admin/login", // Admin login phải public
                                "/api/image/quiz/**",
                                "/api/user/avatars/**",
                                "/api/upload/avatars/**",
                                "/api/quiz/join/**",
                                "/api/quiz/public/**",
                                "/api/quiz/detail/**",
                                "/api/question/**",
                                "/api/quiz-attempts/public/**",
                                "/api/result/submit",
                                "/ws/**", // WEBSOCKET ENDPOINTS
                                "/topic/**", // WEBSOCKET TOPICS
                                "/queue/**" // WEBSOCKET QUEUES
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll() // GET quizzes public
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/review", "/api/user/change-password",
                                "/api/user/verify-password")
                        .hasAnyRole("USER", "ADMIN") // Review requires auth

                        // ADMIN-ONLY ENDPOINTS (trừ login)
                        .requestMatchers("/api/admin/dashboard/**", "/api/admin/users/**", "/api/admin/quizzes/**",
                                "/api/admin/reports/**", "/api/admin/analytics/**", "/api/admin/attempts/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/categories/**").hasRole("ADMIN") // POST/PUT/DELETE categories cần ADMIN

                        // USER ENDPOINTS - Cần authentication
                        .requestMatchers(
                                "/api/user/**",
                                "/api/quiz/user/**",
                                "/api/quiz-attempts/**",
                                "/api/quiz/create-quiz-with-image", // Tạo quiz cần auth
                                "/api/quiz-resume/**", // Resume quiz endpoints
                                "/api/answer/**",
                                "/api/result/**")
                        .hasAnyRole("USER", "ADMIN")

                        // DEFAULT - Tất cả request khác cần authentication
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("Authentication Required: " + authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            String jsonResponse = """
                                    {
                                        "error": "AUTHENTICATION_REQUIRED",
                                        "message": "Bạn cần đăng nhập để truy cập endpoint này",
                                        "userRole": "ROLE_ANONYMOUS",
                                        "endpoint": "%s",
                                        "method": "%s",
                                        "timestamp": "%s"
                                    }
                                    """.formatted(
                                    request.getRequestURI(),
                                    request.getMethod(),
                                    new java.util.Date());

                            response.getWriter().write(jsonResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            System.out.println("Access Denied: " + accessDeniedException.getMessage());

                            // Lấy thông tin user từ SecurityContext
                            String userRole = "ROLE_ANONYMOUS";
                            String username = "anonymous";

                            var authentication = org.springframework.security.core.context.SecurityContextHolder
                                    .getContext().getAuthentication();
                            if (authentication != null && authentication.isAuthenticated() &&
                                    !authentication.getName().equals("anonymousUser")) {
                                username = authentication.getName();
                                var authorities = authentication.getAuthorities();
                                if (!authorities.isEmpty()) {
                                    userRole = authorities.iterator().next().getAuthority();
                                }
                            }

                            System.out.println("Access Denied for user: " + username + " with role: " + userRole);

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            String jsonResponse = """
                                    {
                                        "error": "ACCESS_DENIED",
                                        "message": "Bạn không có quyền truy cập endpoint này. Cần quyền ADMIN.",
                                        "username": "%s",
                                        "userRole": "%s",
                                        "endpoint": "%s",
                                        "method": "%s",
                                        "timestamp": "%s"
                                    }
                                    """.formatted(
                                    username,
                                    userRole,
                                    request.getRequestURI(),
                                    request.getMethod(),
                                    new java.util.Date());

                            response.getWriter().write(jsonResponse);
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(bannedUserFilter, JwtFilter.class);

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
