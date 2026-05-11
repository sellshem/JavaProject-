package kz.qazaqlearn.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class FailedLoginAttemptService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "failed_login:";
    private final int MAX_ATTEMPTS = 5;
    private final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    public FailedLoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Record a failed login attempt for the given email.
     * If the user exceeds MAX_ATTEMPTS, the account will be locked for LOCKOUT_DURATION.
     */
    public void recordFailedAttempt(String email) {
        String key = PREFIX + email.toLowerCase();
        Long attempts = redisTemplate.opsForValue().increment(key);
        
        if (attempts != null && attempts == 1) {
            // First attempt - set expiration
            redisTemplate.expire(key, LOCKOUT_DURATION);
        }
    }

    /**
     * Check if the account is locked due to too many failed login attempts.
     * @param email the email to check
     * @return true if the account is locked, false otherwise
     */
    public boolean isAccountLocked(String email) {
        String key = PREFIX + email.toLowerCase();
        String valueStr = redisTemplate.opsForValue().get(key);
        
        if (valueStr == null) {
            return false;
        }
        
        try {
            int attempts = Integer.parseInt(valueStr);
            return attempts >= MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Reset failed login attempts for the given email.
     * Called after a successful login.
     */
    public void resetAttempts(String email) {
        String key = PREFIX + email.toLowerCase();
        redisTemplate.delete(key);
    }

    /**
     * Get the remaining lockout time in seconds for a locked account.
     * @param email the email to check
     * @return remaining time in seconds, or 0 if not locked
     */
    public long getRemainingLockoutTime(String email) {
        String key = PREFIX + email.toLowerCase();
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * Get the current number of failed attempts for the given email.
     */
    public int getAttemptCount(String email) {
        String key = PREFIX + email.toLowerCase();
        String valueStr = redisTemplate.opsForValue().get(key);
        
        if (valueStr == null) {
            return 0;
        }
        
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}