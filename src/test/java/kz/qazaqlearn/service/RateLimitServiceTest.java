package kz.qazaqlearn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimitService = new RateLimitService(redisTemplate);
    }

    @Test
    void shouldAllowLoginWhenUnderLimit() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean allowed = rateLimitService.isLoginAllowed(ipAddress);

        // Then
        assertThat(allowed).isTrue();
        verify(valueOperations, times(1)).increment("rate_limit:login:" + ipAddress);
    }

    @Test
    void shouldBlockLoginAfterExceedingMaxAttempts() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.increment(anyString())).thenReturn(6L);
        when(valueOperations.get(anyString())).thenReturn("6");

        // When
        boolean allowed = rateLimitService.isLoginAllowed(ipAddress);

        // Then
        assertThat(allowed).isFalse();
    }

    @Test
    void shouldAllowRegisterWhenUnderLimit() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean allowed = rateLimitService.isRegisterAllowed(ipAddress);

        // Then
        assertThat(allowed).isTrue();
    }

    @Test
    void shouldBlockRegisterAfterExceedingMaxAttempts() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.increment(anyString())).thenReturn(4L);
        when(valueOperations.get(anyString())).thenReturn("4");

        // When
        boolean allowed = rateLimitService.isRegisterAllowed(ipAddress);

        // Then
        assertThat(allowed).isFalse();
    }

    @Test
    void shouldAllowApiCallWhenUnderLimit() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean allowed = rateLimitService.isApiCallAllowed(ipAddress);

        // Then
        assertThat(allowed).isTrue();
    }

    @Test
    void shouldReturnRemainingRequestsForLogin() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.get(anyString())).thenReturn("2");

        // When
        int remaining = rateLimitService.getRemainingRequests(ipAddress, "login");

        // Then
        assertThat(remaining).isEqualTo(3); // 5 - 2 = 3
    }

    @Test
    void shouldReturnZeroRemainingRequestsWhenLimitExceeded() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.get(anyString())).thenReturn("10");

        // When
        int remaining = rateLimitService.getRemainingRequests(ipAddress, "login");

        // Then
        assertThat(remaining).isEqualTo(0);
    }

    @Test
    void shouldReturnMaxRequestsWhenNoPreviousRequests() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        int remaining = rateLimitService.getRemainingRequests(ipAddress, "login");

        // Then
        assertThat(remaining).isEqualTo(5);
    }

    @Test
    void shouldReturnResetTimeFromRedis() {
        // Given
        String ipAddress = "192.168.1.1";
        when(redisTemplate.getExpire(anyString())).thenReturn(300L);

        // When
        long resetTime = rateLimitService.getRateLimitResetTime(ipAddress, "login");

        // Then
        assertThat(resetTime).isEqualTo(300L);
    }

    @Test
    void shouldReturnZeroResetTimeWhenNoKeyExists() {
        // Given
        String ipAddress = "192.168.1.1";
        when(redisTemplate.getExpire(anyString())).thenReturn(null);

        // When
        long resetTime = rateLimitService.getRateLimitResetTime(ipAddress, "login");

        // Then
        assertThat(resetTime).isEqualTo(0L);
    }

    @Test
    void shouldResetRateLimit() {
        // Given
        String ipAddress = "192.168.1.1";

        // When
        rateLimitService.resetRateLimit(ipAddress, "login");

        // Then
        verify(redisTemplate, times(1)).delete("rate_limit:login:" + ipAddress);
    }

    @Test
    void shouldReturnCorrectConfigurationValues() {
        // Then
        assertThat(rateLimitService.getMaxLoginAttempts()).isEqualTo(5);
        assertThat(rateLimitService.getLoginWindow()).isEqualTo(Duration.ofMinutes(15));
        assertThat(rateLimitService.getMaxRegisterAttempts()).isEqualTo(3);
        assertThat(rateLimitService.getRegisterWindow()).isEqualTo(Duration.ofHours(1));
        assertThat(rateLimitService.getMaxApiRequests()).isEqualTo(100);
        assertThat(rateLimitService.getApiWindow()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldHandleDifferentEndpointTypesForRemainingRequests() {
        // Given
        String ipAddress = "192.168.1.1";
        when(valueOperations.get(anyString())).thenReturn("1");

        // When & Then
        assertThat(rateLimitService.getRemainingRequests(ipAddress, "login")).isEqualTo(4);
        assertThat(rateLimitService.getRemainingRequests(ipAddress, "register")).isEqualTo(2);
        assertThat(rateLimitService.getRemainingRequests(ipAddress, "api")).isEqualTo(99);
        assertThat(rateLimitService.getRemainingRequests(ipAddress, "unknown")).isEqualTo(99);
    }

    private String anyString() {
        return any(String.class);
    }
}