package com.sns.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/alarm")
            .setAllowedOriginPatterns("*")
            // .addInterceptors(authHandshakeInterceptor)
            .withSockJS();

        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*")
            .addInterceptors(authHandshakeInterceptor)
            // .setHandshakeHandler(new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy())) // Add this
            .withSockJS();
            // .setSessionCookieNeeded(false); // ✅ Disable session cookies

            
    }



    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // from server to client
        registry.setApplicationDestinationPrefixes("/app"); // from client to server
    }
} 