package dev.epsilon.ratelimiter.core.aspect;

import dev.epsilon.ratelimiter.core.annotation.RateLimit;
import dev.epsilon.ratelimiter.core.exception.RateLimitExceededException;
import dev.epsilon.ratelimiter.core.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = resolveKey(rateLimit, joinPoint);

        boolean allowed = rateLimiterService.isAllowed(
            "rate_limit:" + key,
            rateLimit.limit(),
            rateLimit.windowSeconds()
        );

        if (!allowed) {
            throw new RateLimitExceededException(key, rateLimit.limit(), rateLimit.windowSeconds());
        }

        return joinPoint.proceed();
    }

    private String resolveKey(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        if (!rateLimit.key().isEmpty()) {
            return rateLimit.key();
        }

        // берём IP если кастомный ключ не задан
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            String method = joinPoint.getSignature().getName();
            return ip + ":" + method;
        } catch (Exception e) {
            return joinPoint.getSignature().toShortString();
        }
    }
}