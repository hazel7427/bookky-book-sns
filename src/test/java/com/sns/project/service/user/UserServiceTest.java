package com.sns.project.service.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sns.project.domain.user.User;
import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.dto.mail.MailTask;
import com.sns.project.handler.exceptionHandler.exception.badRequest.RegisterFailedException;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundEmailException;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.InvalidPasswordException;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.InvalidEmailTokenException;
import com.sns.project.repository.UserRepository;
import com.sns.project.service.RedisService;
import com.sns.project.config.JwtTokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private RequestRegisterDto registerDto;
    private User user;

    @BeforeEach
    void setUp() {
        // ReflectionTestUtils : private 필드에 접근하기 위해 사용
        ReflectionTestUtils.setField(userService, "domainUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(userService, "templateEngine", templateEngine);
        
        registerDto = RequestRegisterDto.builder()
                .email("test@test.com")
                .password("password123!")
                .name("테스트")
                .build();

        user = User.builder()
                .email("test@test.com")
                .password("hashedPassword")
                .name("테스트")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccess() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        User result = userService.register(registerDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(registerDto.getEmail());
        verify(redisService).putValueInHash(anyString(), anyString(), any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 실패")
    void registerFailWithDuplicateEmail() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(registerDto))
                .isInstanceOf(RegisterFailedException.class);
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 성공 테스트")
    void requestPasswordResetSuccess() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(templateEngine.process(eq("email/password-reset"), any())).thenReturn("이메일 템플릿");

        // when
        userService.requestPasswordReset("test@test.com");

        // then
        verify(redisService).setValueWithExpiration(anyString(), anyString(), anyLong());
        verify(redisService).pushToQueue(anyString(), any(MailTask.class));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 요청 시 실패")
    void requestPasswordResetFailWithInvalidEmail() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.requestPasswordReset("invalid@test.com"))
                .isInstanceOf(NotFoundEmailException.class);
    }

    @Test
    @DisplayName("비밀번호 재설정 성공 테스트")
    void resetPasswordSuccess() {
        // given
        String token = "valid-token";
        String newPassword = "newPassword123!";
        when(redisService.getValue(anyString(), eq(String.class))).thenReturn(Optional.of("test@test.com"));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        userService.resetPassword(token, newPassword);

        // then
        verify(userRepository).save(any(User.class));
        verify(redisService).deleteValue(anyString());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 비밀번호 재설정 시 실패")
    void resetPasswordFailWithInvalidToken() {
        // given
        String token = "invalid-token";
        String newPassword = "newPassword123!";
        when(redisService.getValue(anyString(), eq(String.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(token, newPassword))
                .isInstanceOf(InvalidEmailTokenException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 비밀번호 재설정 시 실패")
    void resetPasswordFailWithNonExistentUser() {
        // given
        String token = "valid-token";
        String newPassword = "newPassword123!";
        when(redisService.getValue(anyString(),eq( String.class))).thenReturn(Optional.of("test@test.com"));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.resetPassword(token, newPassword))
                .isInstanceOf(NotFoundEmailException.class);
    }

    @Test
    @DisplayName("인증 성공 테스트")
    void authenticateSuccess() {
        // given
        String rawPassword = "password123!";
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(rawPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        user.setPassword(hashedPassword);
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt.token.here");

        // when
        String token = userService.authenticate("test@test.com", rawPassword);

        // then
        assertThat(token).isNotNull();
        verify(jwtTokenProvider).generateToken(anyString());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 인증 시 실패")
    void authenticateFailWithInvalidPassword() {
        // given
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw("correctPassword", org.mindrot.jbcrypt.BCrypt.gensalt());
        user.setPassword(hashedPassword);
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.authenticate("test@test.com", "wrongPassword"))
                .isInstanceOf(InvalidPasswordException.class);
    }
} 