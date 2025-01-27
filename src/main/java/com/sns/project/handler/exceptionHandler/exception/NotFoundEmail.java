package com.sns.project.handler.exceptionHandler.exception;

public class NotFoundEmail extends RuntimeException{

  public NotFoundEmail(String email){
    super("not existed email: "+email);
  }
}
