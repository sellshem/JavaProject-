package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.RefreshToken;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.AuthResponse;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.repository.RefreshTokenRepository;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtTokenProvider tokenProvider,
                               UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public String createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        // Delete old refresh tokens
        refreshTokenRepository.deleteByUserId(userId);
        
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(REFRESH_TOKEN_VALIDITY));
        refreshTokenRepository.save(refreshToken);
        
        return token;
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        
        if (tokenEntity.isExpired()) {
            refreshTokenRepository.delete(tokenEntity);
            throw new BadRequestException("Refresh token expired");
        }
        
        User user = tokenEntity.getUser();
        String newAccessToken = tokenProvider.generateToken(
                user.getEmail(), 
                user.getId(), 
                user.getRole().name()
        );
        
        // Rotate refresh token
        String newRefreshToken = createRefreshToken(user.getId());
        
        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", 
                user.getId().toString(), user.getEmail(), user.getRole());
    }

    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}
