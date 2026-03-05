package dev.epsilon.ratelimiter.demo.controller;

import dev.epsilon.ratelimiter.core.exception.RateLimitExceededException;
import dev.epsilon.ratelimiter.demo.entity.RequestLog;
import dev.epsilon.ratelimiter.demo.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final RequestLogRepository requestLogRepository;

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RateLimitExceededException ex) {
        requestLogRepository.save(RequestLog.builder()
                                      .clientKey(ex.getKey())
                                      .endpoint("unknown")
                                      .blockedAt(Instant.now())
                                      .limitValue(ex.getLimit())
                                      .windowSeconds(ex.getWindowSeconds())
                                      .build());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
            "error", "Too Many Requests",
            "message", ex.getMessage(),
            "retryAfter", ex.getWindowSeconds()
        ));
    }
}