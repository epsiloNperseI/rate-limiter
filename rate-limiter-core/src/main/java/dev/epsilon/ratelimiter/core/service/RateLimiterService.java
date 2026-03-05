package dev.epsilon.ratelimiter.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private final RedisScript<Long> slidingWindowScript;

    public boolean isAllowed(String key, int limit, int windowSeconds) {
        long nowMillis = Instant.now().toEpochMilli();

        Long result = redisTemplate.execute(
            slidingWindowScript,
            List.of(key),
            String.valueOf(nowMillis),
            String.valueOf(windowSeconds),
            String.valueOf(limit)
        );

        boolean allowed = Long.valueOf(1L).equals(result);
        log.debug("RateLimit check — key: {}, allowed: {}", key, allowed);
        return allowed;
    }
}