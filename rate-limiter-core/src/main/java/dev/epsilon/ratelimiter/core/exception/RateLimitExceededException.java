package dev.epsilon.ratelimiter.core.exception;

public class RateLimitExceededException extends RuntimeException {

    private final String key;
    private final int limit;
    private final int windowSeconds;

    public RateLimitExceededException(String key, int limit, int windowSeconds) {
        super("Rate limit exceeded for key: %s — %d requests per %ds"
                  .formatted(key, limit, windowSeconds));
        this.key = key;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    public String getKey() { return key; }
    public int getLimit() { return limit; }
    public int getWindowSeconds() { return windowSeconds; }
}
