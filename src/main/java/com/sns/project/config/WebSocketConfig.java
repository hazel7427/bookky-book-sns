package com.sns.project.config;

import com.sns.project.interceptor.StompAuthChannelInterceptor;
import com.sns.project.interceptor.StompDebugInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.MimeTypeUtils;
import java.util.List;

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

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);
        return false;
    }

} 