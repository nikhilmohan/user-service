package com.nikhilm.hourglass.userservice.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiError> handleUserException(UserException e) {
        log.error("Exception " + e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(new ApiError(String.valueOf(e.getStatus()), e.getMessage()));
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception e) {
        log.error("Exception " + e.getMessage() + e.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError("500", e.getMessage()));
    }

}
