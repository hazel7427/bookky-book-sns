package com.sns.project.service.user;

import com.sns.project.domain.user.User;
import com.sns.project.domain.user.UserFactory;
import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundEmailException;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundUserException;
import com.sns.project.handler.exceptionHandler.exception.badRequest.RegisterFailedException;
import com.sns.project.repository.UserRepository;
import com.sns.project.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.core.io.Resource;
import org.thymeleaf.context.Context;
import java.util.UUID;
import com.sns.project.dto.mail.MailTask;
import org.mindrot.jbcrypt.BCrypt;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.InvalidEmailTokenException;
import com.sns.project.config.JwtTokenProvider;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.InvalidPasswordException;
import static com.sns.project.config.constants.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final RedisService redisService;
  @Value("classpath:templates/email/password-reset.html")
  private Resource htmlTemplate;
  @Autowired  
  private SpringTemplateEngine templateEngine;
  @Value("${app.domain.url}")
  private String domainUrl;
  private final JwtTokenProvider jwtTokenProvider;
  
  // 비밀번호 재설정 링크 생성
  private String getBody(String resetPasswordLink) {
      Context context = new Context();
      context.setVariable("resetPasswordLink", resetPasswordLink);      
      return templateEngine.process("email/password-reset", context);
  }

  @Transactional
  public User register(RequestRegisterDto request) {
    validateEmail(request.getEmail());
    
    User newUser = UserFactory.createUser(request);
    newUser.setPassword(hashPassword(newUser.getPassword()));
    
    User registered = saveAndCache(newUser);
    
    log.info("사용자 등록 성공: {}", registered.getEmail());
    return registered;
  }

  private String hashPassword(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  private void validateEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      log.warn("Registration failed: Email already exists - {}", email);
      throw new RegisterFailedException("이미 사용 중인 이메일입니다");
    }
  }

  private User saveAndCache(User user) {
    User registered = userRepository.save(user);
    cacheUserData(registered);
    return registered;
  }

  /*
   * 사용자 데이터 캐싱
   * 해시 키: USER_CACHE_KEY, 
   * (필드 키, 필드 값): (email, user)
   * @param user 사용자 데이터
   */
  private void cacheUserData(User user) {
    try {
      redisService.putValueInHash(UserConstants.USER_CACHE_KEY, user.getEmail(), user);
    } catch (Exception e) {
      log.error("사용자 데이터 캐싱 실패. 사용자: {}, 에러: {}", user.getEmail(), e.getMessage());
    }
  }

  private String createPasswordResetKey(String token) {
    return PasswordResetConstants.RESET_TOKEN_KEY + token;
  }

  /*
   * 비밀번호 재설정 요청
   * @param email 사용자 이메일
   * 
   * 1. db에서 이메일 조회
   * 2. 토큰 생성
   * 3. 레디스에 (key, email) 저장 -> key = PASSWORD_RESET_TOKEN_KEY + token
   * 4. 메일 작업을 Redis 큐에 추가
   */
  @Transactional
  public void requestPasswordReset(String email) {
    userRepository.findByEmail(email)
        .orElseThrow(() -> {
            log.error("비밀번호 재설정 요청 실패: 존재하지 않는 이메일 - {}", email);
            return new NotFoundEmailException(email);
        });
    
    String token = UUID.randomUUID().toString();
    String resetLink = domainUrl + PasswordResetConstants.RESET_PATH + token;
    String passwordResetHashKey = createPasswordResetKey(token);
    
    redisService.setValueWithExpiration(passwordResetHashKey, email, PasswordResetConstants.RESET_EXPIRATION_MINUTES * 60);
    
    MailTask mailTask = MailTask.builder()
        .email(email)
        .content(getBody(resetLink))
        .subject("비밀번호를 재설정하세요")
        .build();

    redisService.pushToQueue(PasswordResetConstants.MAIL_QUEUE_KEY, mailTask);
    log.info("비밀번호 재설정 메일 큐 추가 완료: {}", email);
  }

  /*
   * 비밀번호 재설정
   * @param key PASSWORD_RESET_TOKEN_KEY + token
   * @param newPassword 새로운 비밀번호
   */
  @Transactional
  public void resetPassword(String token, String newPassword) {
    String passwordResetHashKey = createPasswordResetKey(token);
    String email = redisService.getValue(passwordResetHashKey, String.class)
        .orElseThrow(() -> {
            log.error("invalid token : {}", token);
            return new InvalidEmailTokenException(token);
        });

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEmailException(email));

    String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
    user.setPassword(hashed);


    userRepository.save(user);
    redisService.deleteValue(passwordResetHashKey);
  }

  @Transactional
  public String authenticate(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEmailException(email));
    
    if (!BCrypt.checkpw(password, user.getPassword())) {
        throw new InvalidPasswordException();
    }

    return saveAuthToken(user.getId());
  }

  public String saveAuthToken(Long id) {
    String token = jwtTokenProvider.generateToken(String.valueOf(id));
    redisService.setValueWithExpiration(token, String.valueOf(id), AuthConstants.CACHE_DURATION_MINUTES * 60);
    return token;
  }

  public User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundUserException(userId));
  }

  public void isExistUser(Long userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundUserException(userId));
  }

}