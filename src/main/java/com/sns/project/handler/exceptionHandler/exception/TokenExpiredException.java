package com.sns.project.handler.exceptionHandler.exception;

public class TokenExpiredException extends
    RuntimeException {

  TokenExpiredException(String token){
    super("token is expired");
  }
}
