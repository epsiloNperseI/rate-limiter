package dev.epsilon.ratelimiter.demo.controller;

import dev.epsilon.ratelimiter.core.annotation.RateLimit;
import dev.epsilon.ratelimiter.demo.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final RequestLogRepository requestLogRepository;

    @GetMapping("/hello")
    @RateLimit(limit = 5, windowSeconds = 10)
    public ResponseEntity<Map<String, String>> hello() {
        return ResponseEntity.ok(Map.of("message", "Hello!", "timestamp", Instant.now().toString()));
    }

    @GetMapping("/strict")
    @RateLimit(limit = 2, windowSeconds = 30)
    public ResponseEntity<Map<String, String>> strict() {
        return ResponseEntity.ok(Map.of("message", "Strict endpoint — only 2 per 30s"));
    }

    @GetMapping("/logs")
    public ResponseEntity<?> logs() {
        return ResponseEntity.ok(requestLogRepository.findAll());
    }
}