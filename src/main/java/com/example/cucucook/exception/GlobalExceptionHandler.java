package com.example.cucucook.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.cucucook.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ApiResponse<?> handleAllExceptions(Exception ex) {
    ApiResponse<?> response = new ApiResponse<>(false, ex.getMessage(), null, null);
    return response;
  }

}