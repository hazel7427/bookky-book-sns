package com.sns.project.service.user;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.sns.project.domain.user.User;
import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.handler.exceptionHandler.exception.RegisterFailedException;
import com.sns.project.repository.UserRepository;
import com.sns.project.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mindrot.jbcrypt.BCrypt;
import com.sns.project.handler.exceptionHandler.exception.InvalidCredentialsException;
import com.sns.project.handler.exceptionHandler.exception.NotFoundEmailException;
import com.sns.project.config.JwtTokenProvider;
import org.springframework.core.io.Resource;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Optional;
import java.util.UUID;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        RequestRegisterDto request = new RequestRegisterDto();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User registeredUser = userService.register(request);

        assertNotNull(registeredUser);
        assertEquals(request.getEmail(), registeredUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        RequestRegisterDto request = new RequestRegisterDto();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(RegisterFailedException.class, () -> userService.register(request));
    }

    // @Test
    // void testRequestPasswordReset() {
    //     String email = "test@example.com";
    //     when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

    //     userService.requestPasswordReset(email);

    //     verify(redisService, times(1)).setValueWithExpiration(anyString(), eq(email), anyInt());
    // }

    @Test
    void testRequestPasswordResetEmailNotFound() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmailException.class, () -> userService.requestPasswordReset(email));
    }

    @Test
    void testResetPassword() {
        String key = "resetKey";
        String newPassword = "newPassword";
        String email = "test@example.com";
        User user = User.builder()
            .email(email)
            .password(BCrypt.hashpw(newPassword, BCrypt.gensalt()))
            .build();

        when(redisService.getValue(key, String.class)).thenReturn(Optional.of(email));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userService.resetPassword(key, newPassword);

        verify(userRepository, times(1)).save(user);
        verify(redisService, times(1)).deleteValue(key);
        
        // 패스워드가 올바르게 저장되었는지 확인
        User updatedUser = userRepository.findByEmail(email).orElseThrow();
        assertTrue(BCrypt.checkpw(newPassword, updatedUser.getPassword()));
    }

    @Test
    void testAuthenticate() {
        String email = "test@example.com";
        String password = "password";
        User user = User.builder()
            .email(email)
            .password(BCrypt.hashpw(password, BCrypt.gensalt()))
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(email)).thenReturn("token");

        String token = userService.authenticate(email, password);

        assertEquals("token", token);
    }

    @Test
    void testAuthenticateInvalidCredentials() {
        String email = "test@example.com";
        String password = "password";
        User user = User.builder()
            .email(email)
            .password(BCrypt.hashpw("wrongPassword", BCrypt.gensalt()))
            .build();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> userService.authenticate(email, password));
    }
}
