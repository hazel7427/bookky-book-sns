package com.sns.project.service.user;



import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.sns.project.domain.user.User;
import com.sns.project.domain.user.UserFactory;
import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.handler.exception.RegisterFailedException;
import com.sns.project.repository.UserRepository;
import com.sns.project.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RedisService redisService;

  @InjectMocks
  private UserService userService;

  private RequestRegisterDto requestRegisterDto;
  private User mockUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    requestRegisterDto = RequestRegisterDto.builder()
        .email("test@example.com")
        .name("Test User")
        .password("securePassword")
        .build();

    mockUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .name("Test User")
        .password("securePassword")
        .build();
  }

  @Test
  void register_WhenEmailExists_ThrowsException() {
    // Given
    when(userRepository.existsByEmail(requestRegisterDto.getEmail())).thenReturn(true);

    // When & Then
    RegisterFailedException thrown = assertThrows(RegisterFailedException.class,
        () -> userService.register(requestRegisterDto));

    assertEquals("already used email", thrown.getMessage());
    verify(userRepository, times(1)).existsByEmail(requestRegisterDto.getEmail());
    verify(userRepository, never()).save(any());
  }

  @Test
  void register_WhenEmailDoesNotExist_SavesUserAndCaches() {
    // Given
    when(userRepository.existsByEmail(requestRegisterDto.getEmail())).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(mockUser);

    // When
    userService.register(requestRegisterDto);

    // Then
    verify(userRepository, times(1)).existsByEmail(requestRegisterDto.getEmail());
    verify(userRepository, times(1)).save(any(User.class));
    verify(redisService, times(1)).putValueInHash(anyString(), eq(mockUser.getId().toString()), any(User.class));
    verify(redisService, times(1)).putValueInHash(anyString(), eq(mockUser.getName()), eq(mockUser.getEmail()));
  }
}
