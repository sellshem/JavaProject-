package kz.qazaqlearn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.lenient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailedLoginAttemptServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private FailedLoginAttemptService failedLoginAttemptService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        failedLoginAttemptService = new FailedLoginAttemptService(redisTemplate);
    }

    @Test
    void shouldRecordFailedAttempt() {
        // Given
        String email = "test@example.com";
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        failedLoginAttemptService.recordFailedAttempt(email);

        // Then
        verify(valueOperations, times(1)).increment("failed_login:" + email.toLowerCase());
        verify(redisTemplate, times(1)).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldNotSetExpiryOnSubsequentAttempts() {
        // Given
        String email = "test@example.com";
        when(valueOperations.increment(anyString())).thenReturn(2L);

        // When
        failedLoginAttemptService.recordFailedAttempt(email);

        // Then
        verify(valueOperations, times(1)).increment("failed_login:" + email.toLowerCase());
        verify(redisTemplate, times(0)).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldReturnFalseForAccountNotLocked() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn("2");

        // When
        boolean isLocked = failedLoginAttemptService.isAccountLocked(email);

        // Then
        assertThat(isLocked).isFalse();
    }

    @Test
    void shouldReturnTrueForAccountLockedAtExactLimit() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn("5");

        // When
        boolean isLocked = failedLoginAttemptService.isAccountLocked(email);

        // Then
        assertThat(isLocked).isTrue();
    }

    @Test
    void shouldReturnTrueForAccountLockedOverLimit() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn("10");

        // When
        boolean isLocked = failedLoginAttemptService.isAccountLocked(email);

        // Then
        assertThat(isLocked).isTrue();
    }

    @Test
    void shouldReturnFalseForAccountWithNoAttempts() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean isLocked = failedLoginAttemptService.isAccountLocked(email);

        // Then
        assertThat(isLocked).isFalse();
    }

    @Test
    void shouldReturnFalseForInvalidAttemptCount() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn("invalid");

        // When
        boolean isLocked = failedLoginAttemptService.isAccountLocked(email);

        // Then
        assertThat(isLocked).isFalse();
    }

    @Test
    void shouldResetAttemptsOnSuccessfulLogin() {
        // Given
        String email = "test@example.com";

        // When
        failedLoginAttemptService.resetAttempts(email);

        // Then
        verify(redisTemplate, times(1)).delete("failed_login:" + email.toLowerCase());
    }

    @Test
    void shouldReturnRemainingLockoutTime() {
        // Given
        String email = "test@example.com";
        when(redisTemplate.getExpire(anyString())).thenReturn(600L);

        // When
        long remainingTime = failedLoginAttemptService.getRemainingLockoutTime(email);

        // Then
        assertThat(remainingTime).isEqualTo(600L);
    }

    @Test
    void shouldReturnZeroLockoutTimeWhenNotLocked() {
        // Given
        String email = "test@example.com";
        when(redisTemplate.getExpire(anyString())).thenReturn(null);

        // When
        long remainingTime = failedLoginAttemptService.getRemainingLockoutTime(email);

        // Then
        assertThat(remainingTime).isEqualTo(0L);
    }

    @Test
    void shouldReturnZeroLockoutTimeWhenKeyHasNoExpiry() {
        // Given
        String email = "test@example.com";
        when(redisTemplate.getExpire(anyString())).thenReturn(-1L);

        // When
        long remainingTime = failedLoginAttemptService.getRemainingLockoutTime(email);

        // Then
        assertThat(remainingTime).isEqualTo(0L);
    }

    @Test
    void shouldReturnAttemptCount() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn("3");

        // When
        int count = failedLoginAttemptService.getAttemptCount(email);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldReturnZeroAttemptCountWhenNoAttempts() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        int count = failedLoginAttemptService.getAttemptCount(email);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldReturnZeroAttemptCountForInvalidValue() {
        // Given
        String email = "test@example.com";
        when(valueOperations.get(anyString())).thenReturn("invalid");

        // When
        int count = failedLoginAttemptService.getAttemptCount(email);

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldHandleEmailCaseInsensitively() {
        // Given
        String email = "Test@Example.COM";
        when(valueOperations.get(anyString())).thenReturn("2");

        // When
        boolean isLocked = failedLoginAttemptService.isAccountLocked(email);
        int count = failedLoginAttemptService.getAttemptCount(email);

        // Then
        // Verify the key is normalized to lowercase - called twice (isAccountLocked + getAttemptCount)
        verify(valueOperations, times(2)).get("failed_login:" + email.toLowerCase());
        assertThat(isLocked).isFalse();
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldIncrementAttemptCountOnMultipleFailures() {
        // Given
        String email = "test@example.com";
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);

        // When
        failedLoginAttemptService.recordFailedAttempt(email);
        failedLoginAttemptService.recordFailedAttempt(email);
        failedLoginAttemptService.recordFailedAttempt(email);

        // Then
        verify(valueOperations, times(3)).increment("failed_login:" + email.toLowerCase());
    }
}