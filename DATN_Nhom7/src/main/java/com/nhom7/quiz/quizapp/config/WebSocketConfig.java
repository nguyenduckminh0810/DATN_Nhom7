package com.nhom7.quiz.quizapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // CẤU HÌNH MESSAGE BROKER
        config.enableSimpleBroker("/topic", "/queue");

        // PREFIX CHO CLIENT GỬI MESSAGE
        config.setApplicationDestinationPrefixes("/app");

        // PREFIX CHO USER-SPECIFIC MESSAGES
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // REGISTER WEBSOCKET ENDPOINT
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CHO PHÉP CORS
                .withSockJS(); // FALLBACK CHO BROWSER KHÔNG HỖ TRỢ WEBSOCKET
    }
}