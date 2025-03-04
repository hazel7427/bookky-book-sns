package com.sns.project.controller.chat;

import com.sns.project.config.aspect.userAuth.AuthRequired;
import com.sns.project.config.aspect.userAuth.UserContext;
import com.sns.project.domain.user.User;
import com.sns.project.controller.chat.dto.request.ChatRoomRequest;
import com.sns.project.controller.chat.dto.response.ChatMessageResponse;
import com.sns.project.controller.chat.dto.response.ChatRoomResponse;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.service.chat.ChatReadService;
import com.sns.project.service.chat.ChatRoomService;
import com.sns.project.service.chat.ChatService;
import com.sns.project.service.user.UserService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")  // Allow requests from frontend

public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ChatService chatService;
    private final ChatReadService chatReadService;

    @PostMapping("/room")
    @AuthRequired
    public ApiResult<ChatRoomResponse> createRoom(@RequestBody ChatRoomRequest chatRoomRequest) {
        Long userId = UserContext.getUserId();
        User creator = userService.getUserById(userId);

        return ApiResult.success(chatRoomService.createRoom(
            chatRoomRequest.getName(),
            chatRoomRequest.getUserIds(),
            creator
        ));
    }

    @GetMapping("/rooms")
    @AuthRequired
    public ApiResult<List<ChatRoomResponse>> getUserChatRooms() {
        Long userId = UserContext.getUserId();
        User user = userService.getUserById(userId);

        return ApiResult.success(chatRoomService.getUserChatRooms(user));
    }

    @GetMapping("/history/{roomId}")
    public List<ChatMessageResponse> getChatHistory(@PathVariable Long roomId) {
        System.out.println("✅ [DEBUG] ChatRoomController getChatHistory 호출");
        return chatService.getChatHistory(roomId);
    }
    

    @PostMapping("/read/{messageId}")
    @AuthRequired
    public ApiResult<Long> markAsRead(@PathVariable Long messageId) {
        Long userId = UserContext.getUserId();
        chatReadService.markMessageAsRead(userId, messageId);
        return ApiResult.success(messageId);
    }
    
}
