package com.guarani.pos.shared.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "FORBIDDEN",
                        "message", ex.getMessage()
                ));
    }

}
