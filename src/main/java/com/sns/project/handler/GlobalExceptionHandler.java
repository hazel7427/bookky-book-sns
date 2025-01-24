package com.sns.project.handler;

import com.sns.project.handler.exception.RegisterFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RegisterFailedException.class)
  public ResponseEntity<?> handleCustomAppException(RegisterFailedException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()));
  }

  /**
   * 데이터 무결성 예외 처리
   * @param ex DataIntegrityViolationException
   * @return ResponseEntity
   */
  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
      org.springframework.dao.DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("data integrity error", HttpStatus.CONFLICT.value()));
  }

  /**
   * 일반적인 예외 처리
   * @param ex Exception
   * @return ResponseEntity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("server error", HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }


  public static class ErrorResponse {
    private final String message;
    private final int status;

    public ErrorResponse(String message, int status) {
      this.message = message;
      this.status = status;
    }

    public String getMessage() {
      return message;
    }

    public int getStatus() {
      return status;
    }
  }
}
