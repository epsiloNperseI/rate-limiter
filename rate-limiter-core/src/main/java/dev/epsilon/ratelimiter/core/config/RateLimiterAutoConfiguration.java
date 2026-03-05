package dev.epsilon.ratelimiter.core.config;

import dev.epsilon.ratelimiter.core.aspect.RateLimitAspect;
import dev.epsilon.ratelimiter.core.service.RateLimiterService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate.class)
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisScript<Long> slidingWindowScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(
            new ResourceScriptSource(new ClassPathResource("scripts/sliding_window.lua"))
        );
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterService rateLimiterService(StringRedisTemplate redisTemplate,
                                                 RedisScript<Long> slidingWindowScript) {
        return new RateLimiterService(redisTemplate, slidingWindowScript);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RateLimiterService rateLimiterService) {
        return new RateLimitAspect(rateLimiterService);
    }
}