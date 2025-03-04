package com.sns.project.config;

import com.sns.project.config.interceptor.StompAuthChannelInterceptor;
import com.sns.project.config.interceptor.StompDebugInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


import lombok.AllArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompDebugInterceptor stompDebugInterceptor;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/alarm")
            .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // from server to client
        registry.setApplicationDestinationPrefixes("/app"); // from client to server
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        System.out.println("✅ [DEBUG] STOMP 메시지 채널 인터셉터 추가됨");
        registration.interceptors(stompAuthChannelInterceptor);
    }

} 