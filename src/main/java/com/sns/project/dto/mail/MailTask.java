package com.sns.project.dto.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MailTask implements Serializable {
    private String email;
    private String subject;
    private String content;
} 