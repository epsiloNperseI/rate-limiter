package dev.epsilon.ratelimiter.demo.repository;

import dev.epsilon.ratelimiter.demo.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    List<RequestLog> findByClientKeyAndBlockedAtAfter(String clientKey, Instant after);
}