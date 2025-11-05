package com.mykms.controller;

import com.mykms.dto.ErrorResponse;
import com.mykms.service.ShutdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 글로벌 예외 핸들러
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ShutdownService shutdownService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());

        // 키를 찾을 수 없는 경우 404 반환
        if (e.getMessage().contains("찾을 수 없습니다")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }

        // 이미 존재하는 경우 409 반환
        if (e.getMessage().contains("이미 존재합니다")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Authentication failed: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("인증에 실패했습니다"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error", e);

        // 치명적인 오류의 경우 긴급 종료 정보 저장
        if (isCriticalException(e)) {
            log.error("치명적인 오류 감지 - 긴급 종료 정보 저장");
            shutdownService.saveEmergencyInfo("Critical Exception: " + e.getClass().getSimpleName(), e);
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("내부 서버 오류가 발생했습니다: " + e.getMessage()));
    }

    /**
     * 치명적인 예외인지 판단
     */
    private boolean isCriticalException(Exception e) {
        // OutOfMemoryError, StackOverflowError 등 치명적인 오류
        if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
            return true;
        }

        // NullPointerException, ClassCastException 등 예상치 못한 런타임 오류
        if (e instanceof NullPointerException || e instanceof ClassCastException) {
            return true;
        }

        // 데이터베이스 관련 심각한 오류
        String message = e.getMessage();
        if (message != null && (message.contains("database") || message.contains("connection"))) {
            return true;
        }

        return false;
    }
}
