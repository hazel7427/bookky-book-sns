package com.sns.project.config.interceptor;

import com.sns.project.service.user.UserService;
import com.sns.project.service.user.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);


        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> protocols = accessor.getNativeHeader("Authorization");

            log.info("Protocols: {}", protocols);
            if (protocols != null) {
                protocols.forEach(protocol -> log.info("Protocol value: {}", protocol));
            }
            
            if (protocols != null && !protocols.isEmpty()) {
                // The token might be in the first protocol string, split by comma
                String[] protocolValues = protocols.get(0).split(",");
                log.info("Split protocol values: {}", (Object) protocolValues);
                
                if (protocolValues.length > 1) {
                    String token = protocolValues[1].trim();  // Get the token and remove whitespace
                    log.info("Token received: {}", token);

                    try {
                        Long userId = tokenService.validateToken(token);
                        accessor.setUser(() -> String.valueOf(userId));
                        log.info("User authenticated: {}", userId);
                    } catch (Exception e) {
                        log.error("Token validation failed: {}", e.getMessage());
                        return null; // This will prevent the connection
                    }
                }
            }
        }
        return message;
    }
}