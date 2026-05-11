package kz.qazaqlearn.dto;

import kz.qazaqlearn.domain.Role;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    String userId,
    String email,
    Role role
) {
}
