package com.sns.project.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.sns.project.service.user.TokenService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Map;
import java.util.Optional;


@Component
@AllArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;

    @Override   
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Extract authentication information from the request
        // For example, check headers or query parameters
        String authToken = extractTokenFromQuery(request.getURI());

        // Validate the token (this is just a placeholder for your logic)
        if (authToken != null && validateToken(authToken)) {
            return true; // Allow the handshake
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        System.out.println("not authorized");
        return false; // Reject the handshake
    }

    @Override       
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }

    private boolean validateToken(String token) {
        // Implement your token validation logic here
        return tokenService.isValidToken(token);
    }

    private String extractTokenFromQuery(URI uri) {
        return Optional.ofNullable(uri.getQuery())
            .map(query -> {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("token")) {
                        return pair[1];
                    }
                }
                return null;
            })
            .orElse(null);
    }
    
}
