package com.sns.project.interceptor;

import org.springframework.stereotype.Component;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompDebugInterceptor implements ChannelInterceptor {

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    System.out.println("✅ [DEBUG] STOMP 메시지 수신: " + accessor.getCommand());
    System.out.println("✅ [DEBUG] 헤더 정보: " + accessor.toNativeHeaderMap());
    System.out.println("✅ [DEBUG] 메시지 페이로드: " + new String((byte[]) message.getPayload()));

    return message;
  }
}

