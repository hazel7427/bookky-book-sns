package com.sns.project.handler.exceptionHandler;

import com.sns.project.handler.exceptionHandler.exception.InvalidCredentialsException;
import com.sns.project.handler.exceptionHandler.exception.NotFoundEmail;
import com.sns.project.handler.exceptionHandler.exception.RegisterFailedException;
import com.sns.project.handler.exceptionHandler.exception.TokenExpiredException;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ApiResult<?>> newResponse(Throwable throwable, HttpStatus httpStatus) {
    logger.error("Exception handled: {}, Status: {}", throwable.getMessage(), httpStatus);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity<>(ApiResult.error(throwable, httpStatus), headers, httpStatus);
  }

  @ExceptionHandler(RegisterFailedException.class)
  public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
    return newResponse(ex, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({
      InvalidCredentialsException.class, TokenExpiredException.class
  })
  public ResponseEntity<ApiResult<?>> handleInvalidCredentials(InvalidCredentialsException ex) {
    return newResponse(ex, HttpStatus.UNAUTHORIZED);
  }



  @ExceptionHandler({
      NotFoundEmail.class
  })
  public ResponseEntity<ApiResult<?>> handleNotFoundException(RuntimeException ex) {
    return newResponse(ex, HttpStatus.NOT_FOUND);
  }

  /**
   * 일반적인 예외 처리
   * @param ex Exception
   * @return ResponseEntity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResult<?>> handleGeneralException(Exception ex) {
    return newResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }



}
