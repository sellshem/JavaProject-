package kz.qazaqlearn.service;

import kz.qazaqlearn.domain.Role;
import kz.qazaqlearn.domain.User;
import kz.qazaqlearn.dto.AuthLoginRequest;
import kz.qazaqlearn.dto.AuthRegisterRequest;
import kz.qazaqlearn.exception.BadRequestException;
import kz.qazaqlearn.repository.UserRepository;
import kz.qazaqlearn.security.JwtTokenProvider;
import kz.qazaqlearn.service.AuditLogService;
import kz.qazaqlearn.service.FailedLoginAttemptService;
import kz.qazaqlearn.service.RefreshTokenService;
import kz.qazaqlearn.service.events.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private FailedLoginAttemptService failedLoginAttemptService;

    @InjectMocks
    private AuthService authService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtTokenProvider, auditLogService, kafkaEventPublisher, refreshTokenService, failedLoginAttemptService);
    }

    @Test
    void registerShouldCreateNewUser() {
        AuthRegisterRequest request = new AuthRegisterRequest("Aidar","aidar@example.com","password","STUDENT");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(jwtTokenProvider.generateToken(anyString(), any(UUID.class), anyString())).thenReturn("token");
        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn("refresh-token-123");

        var response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.role()).isEqualTo(Role.STUDENT);
        verify(userRepository).save(any(User.class));
        verify(kafkaEventPublisher).publishUserRegisteredEvent(any());
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        AuthRegisterRequest request = new AuthRegisterRequest("Aidar","aidar@example.com","password","STUDENT");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        AuthLoginRequest request = new AuthLoginRequest("aidar@example.com","password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.STUDENT);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(anyString(), any(UUID.class), anyString())).thenReturn("token");
        when(refreshTokenService.createRefreshToken(any(UUID.class))).thenReturn("refresh-token-123");

        var response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.email()).isEqualTo(request.email());
        verify(kafkaEventPublisher).publishUserRegisteredEvent(any());
    }
}
