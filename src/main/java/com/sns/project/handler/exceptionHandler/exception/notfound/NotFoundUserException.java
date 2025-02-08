package com.sns.project.handler.exceptionHandler.exception.notfound;



public class NotFoundUserException extends RuntimeException{

    public NotFoundUserException(Long userId){
      super("not existed user: "+userId);
    }
  }