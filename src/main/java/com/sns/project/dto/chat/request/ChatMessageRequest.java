package com.sns.project.dto.chat.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChatMessageRequest {
    @NotBlank(message = "Message cannot be empty")  // Ensures message is not null or empty
    private String message;
}
