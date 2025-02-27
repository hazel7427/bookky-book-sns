package com.sns.project.config;

import com.sns.project.service.user.TokenService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String authToken = extractTokenFromQuery(request.getURI());
        if (authToken == null) {
            return false; // 인증 실패 시 WebSocket 연결 차단
        }

        Long userId = tokenService.validateToken(authToken);
        attributes.put("userId", userId); // WebSocket 세션에 사용자 정보 저장
        return true;
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


    @Override       
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }

    private Optional<String> extractTokenFromHeaders(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst("Authorization"))
            .filter(authHeader -> authHeader.startsWith("Bearer "))
            .map(authHeader -> authHeader.substring(7)); // "Bearer " 부분 제거 후 반환
    }
}
