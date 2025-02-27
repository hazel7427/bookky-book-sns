package com.sns.project.controller.chat;

import com.sns.project.aspect.userAuth.AuthRequired;
import com.sns.project.aspect.userAuth.UserContext;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.user.User;
import com.sns.project.dto.chat.request.ChatRoomRequest;
import com.sns.project.dto.chat.response.ChatRoomResponse;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.service.chat.ChatRoomService;
import com.sns.project.service.user.UserService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;

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
}
