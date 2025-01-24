package com.sns.project.service.user;

import static com.sns.project.config.Constants.USER_CACHE_KEY;

import com.sns.project.domain.user.User;
import com.sns.project.domain.user.UserFactory;
import com.sns.project.dto.user.request.RequestRegisterDto;
import com.sns.project.handler.exception.RegisterFailedException;
import com.sns.project.repository.UserRepository;
import com.sns.project.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RedisService redisService;

  @Transactional
  public void register(RequestRegisterDto request){
    String email = request.getEmail();
    if(userRepository.existsByEmail(email)){
      throw new RegisterFailedException("already used email");
    }

    save(request);
  }


  @Transactional
  public User save(RequestRegisterDto request) {
    User newUser = UserFactory.createUser(request);
    User registered = userRepository.save(newUser);

    // 사용자 데이터를 빠르게 가져오기 위해 캐싱
    // 키: 아이디(이메일), 값: 사용자 객체
    redisService.putValueInHash(USER_CACHE_KEY, registered.getId().toString(), registered);
    // 키: 이름, 값: 아이디(이메일)
    redisService.putValueInHash(USER_CACHE_KEY, registered.getName(), registered.getEmail());

    return registered;
  }
}
