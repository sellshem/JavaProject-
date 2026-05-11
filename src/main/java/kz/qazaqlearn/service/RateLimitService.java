package kz.qazaqlearn.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    
    // Rate limiting prefixes
    private static final String LOGIN_PREFIX = "rate_limit:login:";
    private static final String API_PREFIX = "rate_limit:api:";
    private static final String REGISTER_PREFIX = "rate_limit:register:";
    
    // Login rate limiting configuration
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    
    // Register rate limiting configuration
    private static final int MAX_REGISTER_ATTEMPTS = 3;
    private static final Duration REGISTER_WINDOW = Duration.ofHours(1);
    
    // General API rate limiting configuration
    private static final int MAX_API_REQUESTS = 100;
    private static final Duration API_WINDOW = Duration.ofMinutes(1);

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if login is allowed for the given IP address.
     * @param ipAddress the client IP address
     * @return true if login is allowed, false if rate limited
     */
    public boolean isLoginAllowed(String ipAddress) {
        return isAllowed(LOGIN_PREFIX + ipAddress, MAX_LOGIN_ATTEMPTS, LOGIN_WINDOW);
    }

    /**
     * Check if registration is allowed for the given IP address.
     * @param ipAddress the client IP address
     * @return true if registration is allowed, false if rate limited
     */
    public boolean isRegisterAllowed(String ipAddress) {
        return isAllowed(REGISTER_PREFIX + ipAddress, MAX_REGISTER_ATTEMPTS, REGISTER_WINDOW);
    }

    /**
     * Check if general API call is allowed for the given IP address.
     * @param ipAddress the client IP address
     * @return true if API call is allowed, false if rate limited
     */
    public boolean isApiCallAllowed(String ipAddress) {
        return isAllowed(API_PREFIX + ipAddress, MAX_API_REQUESTS, API_WINDOW);
    }

    /**
     * Generic rate limiting check using sliding window algorithm with Redis.
     * @param key the Redis key for rate limiting
     * @param maxRequests maximum number of requests allowed in the window
     * @param window the time window for rate limiting
     * @return true if the request is allowed, false if rate limited
     */
    private boolean isAllowed(String key, int maxRequests, Duration window) {
        Long current = redisTemplate.opsForValue().increment(key);
        
        if (current != null && current == 1) {
            // First request - set expiration
            redisTemplate.expire(key, window.toMillis(), TimeUnit.MILLISECONDS);
        }
        
        return current != null && current <= maxRequests;
    }

    /**
     * Get the remaining number of requests allowed in the current window.
     * @param ipAddress the client IP address
     * @param endpointType the type of endpoint (login, register, api)
     * @return remaining requests, or 0 if rate limited
     */
    public int getRemainingRequests(String ipAddress, String endpointType) {
        String key = switch (endpointType.toLowerCase()) {
            case "login" -> LOGIN_PREFIX + ipAddress;
            case "register" -> REGISTER_PREFIX + ipAddress;
            default -> API_PREFIX + ipAddress;
        };
        
        String currentStr = redisTemplate.opsForValue().get(key);
        if (currentStr == null) {
            return getMaxRequests(endpointType);
        }
        
        try {
            int current = Integer.parseInt(currentStr);
            int max = getMaxRequests(endpointType);
            return Math.max(0, max - current);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get the maximum requests allowed for the given endpoint type.
     */
    private int getMaxRequests(String endpointType) {
        return switch (endpointType.toLowerCase()) {
            case "login" -> MAX_LOGIN_ATTEMPTS;
            case "register" -> MAX_REGISTER_ATTEMPTS;
            default -> MAX_API_REQUESTS;
        };
    }

    /**
     * Get the time until the rate limit resets in seconds.
     * @param ipAddress the client IP address
     * @param endpointType the type of endpoint
     * @return time in seconds until reset, or 0 if no limit active
     */
    public long getRateLimitResetTime(String ipAddress, String endpointType) {
        String key = switch (endpointType.toLowerCase()) {
            case "login" -> LOGIN_PREFIX + ipAddress;
            case "register" -> REGISTER_PREFIX + ipAddress;
            default -> API_PREFIX + ipAddress;
        };
        
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * Reset rate limit for a specific IP and endpoint type.
     * @param ipAddress the client IP address
     * @param endpointType the type of endpoint
     */
    public void resetRateLimit(String ipAddress, String endpointType) {
        String key = switch (endpointType.toLowerCase()) {
            case "login" -> LOGIN_PREFIX + ipAddress;
            case "register" -> REGISTER_PREFIX + ipAddress;
            default -> API_PREFIX + ipAddress;
        };
        
        redisTemplate.delete(key);
    }

    // Getters for configuration values (useful for monitoring/debugging)
    public int getMaxLoginAttempts() {
        return MAX_LOGIN_ATTEMPTS;
    }

    public Duration getLoginWindow() {
        return LOGIN_WINDOW;
    }

    public int getMaxRegisterAttempts() {
        return MAX_REGISTER_ATTEMPTS;
    }

    public Duration getRegisterWindow() {
        return REGISTER_WINDOW;
    }

    public int getMaxApiRequests() {
        return MAX_API_REQUESTS;
    }

    public Duration getApiWindow() {
        return API_WINDOW;
    }
}