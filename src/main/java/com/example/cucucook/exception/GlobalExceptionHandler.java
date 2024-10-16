package com.example.cucucook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.example.cucucook.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
    ApiResponse<?> response = new ApiResponse<>(false, "E_MAX_UPLOAD_SIZE", null, null);
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception ex) {
    ApiResponse<?> response = new ApiResponse<>(false, ex.getMessage(), null, null);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

}