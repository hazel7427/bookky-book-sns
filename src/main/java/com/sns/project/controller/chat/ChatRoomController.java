package com.sns.project.controller.chat;

import com.sns.project.aspect.userAuth.AuthRequired;
import com.sns.project.aspect.userAuth.UserContext;
import com.sns.project.domain.chat.ChatMessage;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import com.sns.project.dto.chat.request.ChatRoomRequest;
import com.sns.project.dto.chat.response.ChatMessageResponse;
import com.sns.project.dto.chat.response.ChatRoomResponse;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
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
    
}
