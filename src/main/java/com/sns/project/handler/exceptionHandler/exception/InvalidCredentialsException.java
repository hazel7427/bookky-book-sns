package com.sns.project.handler.exceptionHandler.exception;

public class InvalidCredentialsException extends
    RuntimeException {
  public InvalidCredentialsException(){
    super("password is invalid");
  }
}
