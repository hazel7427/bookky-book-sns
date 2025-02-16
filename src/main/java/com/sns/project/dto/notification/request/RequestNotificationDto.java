
package com.sns.project.dto.notification.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestNotificationDto {
    @NotNull
    private Long userId;
    @NotNull
    private String message;
}
