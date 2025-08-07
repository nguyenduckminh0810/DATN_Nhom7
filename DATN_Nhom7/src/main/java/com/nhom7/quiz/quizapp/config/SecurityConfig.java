package com.nhom7.quiz.quizapp.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.lang.NonNull;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                        .requestMatchers(
                                "/api/login",
                                "/api/register",
                                "/api/admin/login",
                                "/api/image/quiz/**",
                                "/api/user/avatars/**",
                                "/api/upload/avatars/**",
                                "/api/quiz/join/**",
                                "/api/quiz/public/**",
                                "/api/quiz/detail/**",
                                "/api/question/**",
                                "/api/quiz-attempts/public/**",
                                "/api/result/submit",
                                "/ws/**",
                                "/topic/**",
                                "/queue/**", "/api/leaderboard/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/review").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(
                                "/api/admin/dashboard/**", "/api/admin/users/**", "/api/admin/quizzes/**",
                                "/api/admin/reports/**", "/api/admin/analytics/**", "/api/admin/attempts/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/api/user/**",
                                "/api/quiz/user/**",
                                "/api/quiz-attempts/**",
                                "/api/quiz/create-quiz-with-image",
                                "/api/answer/**",
                                "/api/result/**")
                        .hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
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
