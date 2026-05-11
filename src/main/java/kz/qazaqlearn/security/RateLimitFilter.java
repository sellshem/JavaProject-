package kz.qazaqlearn.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.qazaqlearn.service.RateLimitService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Rate limiting filter that prevents abuse of API endpoints.
 * This filter:
 * - Ignores OPTIONS requests (CORS preflight)
 * - Applies strict rate limiting to authentication endpoints
 * - Applies general rate limiting to all API endpoints
 * - Returns appropriate HTTP 429 responses with retry information
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private final RateLimitService rateLimitService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    // Paths that should be excluded from rate limiting (except OPTIONS which is always excluded)
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/actuator/**",
        "/api/debug/**",
        "/v3/api-docs/**",
        "/swagger-ui/**"
    );

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIpAddress(request);
        
        // Always allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Skipping rate limiting for OPTIONS request: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Skip rate limiting for excluded paths
        if (isExcludedPath(path)) {
            log.debug("Skipping rate limiting for excluded path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check authentication endpoint rate limits
        if (isAuthEndpoint(path)) {
            if (!handleAuthRateLimit(path, method, clientIp, response)) {
                return;
            }
        }
        
        // Check general API rate limit
        if (path.startsWith("/api/")) {
            if (!rateLimitService.isApiCallAllowed(clientIp)) {
                handleRateLimitExceeded(response, "api", clientIp);
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Handle rate limiting for authentication endpoints.
     */
    private boolean handleAuthRateLimit(String path, String method, String clientIp, HttpServletResponse response) 
            throws IOException {
        
        if (isLoginEndpoint(path) && "POST".equalsIgnoreCase(method)) {
            if (!rateLimitService.isLoginAllowed(clientIp)) {
                handleRateLimitExceeded(response, "login", clientIp);
                return false;
            }
        }
        
        if (isRegisterEndpoint(path) && "POST".equalsIgnoreCase(method)) {
            if (!rateLimitService.isRegisterAllowed(clientIp)) {
                handleRateLimitExceeded(response, "register", clientIp);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Handle rate limit exceeded response.
     */
    private void handleRateLimitExceeded(HttpServletResponse response, String endpointType, String clientIp) 
            throws IOException {
        
        long resetTime = rateLimitService.getRateLimitResetTime(clientIp, endpointType);
        int remaining = rateLimitService.getRemainingRequests(clientIp, endpointType);
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("X-RateLimit-Limit", String.valueOf(getLimit(endpointType)));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + resetTime * 1000));
        response.setHeader("Retry-After", String.valueOf(resetTime));
        
        String message = switch (endpointType) {
            case "login" -> "Too many login attempts. Please try again later.";
            case "register" -> "Too many registration attempts. Please try again later.";
            default -> "Too many requests. Please try again later.";
        };
        
        String jsonResponse = String.format(
            "{\"error\":\"rate_limit_exceeded\",\"message\":\"%s\",\"retryAfter\":%d}",
            message, resetTime
        );
        
        response.getWriter().write(jsonResponse);
        log.warn("Rate limit exceeded for IP: {}, endpoint: {}", clientIp, endpointType);
    }
    
    /**
     * Get the rate limit for the given endpoint type.
     */
    private int getLimit(String endpointType) {
        return switch (endpointType) {
            case "login" -> rateLimitService.getMaxLoginAttempts();
            case "register" -> rateLimitService.getMaxRegisterAttempts();
            default -> rateLimitService.getMaxApiRequests();
        };
    }
    
    /**
     * Check if the path is an excluded path.
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    /**
     * Check if the path is a login endpoint.
     */
    private boolean isLoginEndpoint(String path) {
        return path.startsWith("/api/auth/login") || path.endsWith("/login");
    }
    
    /**
     * Check if the path is a register endpoint.
     */
    private boolean isRegisterEndpoint(String path) {
        return path.startsWith("/api/auth/register") || path.endsWith("/register");
    }
    
    /**
     * Check if the path is any authentication endpoint.
     */
    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth/") || path.startsWith("/api/users/auth/");
    }
    
    /**
     * Extract client IP address from request, handling proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", 
                           "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
}