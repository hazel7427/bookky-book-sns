package com.sns.project.service.user;

import static com.sns.project.config.Constants.PASSWORD_RESET_TOKEN_KEY;
import static com.sns.project.config.Constants.USER_CACHE_KEY;

import com.sns.project.domain.user.User;
import com.sns.project.domain.user.UserFactory;
import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.handler.exceptionHandler.exception.NotFoundEmailException;
import com.sns.project.handler.exceptionHandler.exception.RegisterFailedException;
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
import com.sns.project.handler.exceptionHandler.exception.InvalidEmailTokenException;
import com.sns.project.config.JwtTokenProvider;
import com.sns.project.handler.exceptionHandler.exception.InvalidCredentialsException;


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
    newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
    
    User registered = saveAndCache(newUser);
    
    log.info("User registered successfully: {}", registered.getEmail());
    return registered;
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
   * (필드 키, 필드 값): (email, user), (name, email)
   * @param user 사용자 데이터
   */
  private void cacheUserData(User user) {
    try {
      redisService.putValueInHash(USER_CACHE_KEY, user.getEmail(), user);
//      redisService.putValueInHash(USER_CACHE_KEY, user.getName(), user.getEmail());
    } catch (Exception e) {
      log.error("Failed to cache user data: {}", e.getMessage());
    }
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
        .orElseThrow(() -> new NotFoundEmailException(email));
    
    String token = UUID.randomUUID().toString();
    // 서버에서 해당 페이지를 렌더링할지 아님 클라이언트에서 렌더링할지 모르것다
    String resetLink = domainUrl + "/reset-password?token=" + token;
        
    String key = PASSWORD_RESET_TOKEN_KEY + token;
    log.info("key: {}", key);
    redisService.setValueWithExpiration(key,  email, 30 * 60);
        
    // 메일 작업을 Redis 큐에 추가
    MailTask mailTask = new MailTask(
        email,
        "requset password reset",
        getBody(resetLink),
        token
    );

//    redisService.pushToList(MAIL_QUEUE_KEY, mailTask);
    log.info("Password reset mail task queued for: {}", email);
  }

  /*
   * 비밀번호 재설정
   * @param key PASSWORD_RESET_TOKEN_KEY + token
   * @param newPassword 새로운 비밀번호
   */
  @Transactional
  public void resetPassword(String key, String newPassword) {
    String email = redisService.getValue( key , String.class)
        .orElseThrow(() -> {
            log.error("invalid key : {}", key);
            return new InvalidEmailTokenException(key);
        });

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEmailException(email));

    String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
    user.setPassword(hashed);


    userRepository.save(user);
//    redisService.deleteValue(key);
  }

  @Transactional
  public String authenticate(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEmailException(email));
        
    log.info("DB 저장된 해시: {}", user.getPassword());

    if (!BCrypt.checkpw(password, user.getPassword())) {
      throw new InvalidCredentialsException();
    }

    return jwtTokenProvider.generateToken(user.getEmail());
  }

}