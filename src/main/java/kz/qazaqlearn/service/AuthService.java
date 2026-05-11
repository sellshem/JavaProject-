package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.domain.events.UserRegisteredEvent;
import kz.qazaqlearn.dto.AuthLoginRequest;
import kz.qazaqlearn.dto.AuthRegisterRequest;
import kz.qazaqlearn.dto.AuthResponse;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.JwtTokenProvider;
import kz.qazaqlearn.service.AuditLogService;
import kz.qazaqlearn.service.FailedLoginAttemptService;
import kz.qazaqlearn.service.RefreshTokenService;
import kz.qazaqlearn.service.events.KafkaEventPublisher;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final RefreshTokenService refreshTokenService;
    private final FailedLoginAttemptService failedLoginAttemptService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService,
                       KafkaEventPublisher kafkaEventPublisher,
                       RefreshTokenService refreshTokenService,
                       FailedLoginAttemptService failedLoginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.refreshTokenService = refreshTokenService;
        this.failedLoginAttemptService = failedLoginAttemptService;
    }

    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role provided");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);

        user = userRepository.save(user);
        String accessToken = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        auditLogService.logAction(user, "USER_REGISTER", "User", user.getId(), null);
        
        // Publish Kafka event
        UserRegisteredEvent event = new UserRegisteredEvent(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            LocalDateTime.now()
        );
        kafkaEventPublisher.publishUserRegisteredEvent(event);

        return new AuthResponse(accessToken, refreshToken, "Bearer", user.getId().toString(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(AuthLoginRequest request) {
        // Check if account is locked
        if (failedLoginAttemptService.isAccountLocked(request.email())) {
            throw new BadRequestException("Account temporarily locked due to too many failed attempts. Try again in 15 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BadRequestException("Invalid credentials"));

            String accessToken = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // Reset failed attempts on successful login
            failedLoginAttemptService.resetAttempts(request.email());

            auditLogService.logAction(user, "USER_LOGIN", "User", user.getId(), null);
            
            // Publish Kafka event
            UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                LocalDateTime.now()
            );
            kafkaEventPublisher.publishUserRegisteredEvent(event);

            return new AuthResponse(accessToken, refreshToken, "Bearer", 
                    user.getId().toString(), user.getEmail(), user.getRole());
        } catch (AuthenticationException e) {
            // Record failed attempt
            failedLoginAttemptService.recordFailedAttempt(request.email());
            throw e;
        }
    }
}
