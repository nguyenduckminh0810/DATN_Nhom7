package com.nhom7.quiz.quizapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Static resource handler for file uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        System.out.println("Configured static resource handler for /uploads/**");
    }
}