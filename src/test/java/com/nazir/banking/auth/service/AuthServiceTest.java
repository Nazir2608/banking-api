package com.nazir.banking.auth.service;

import com.nazir.banking.auth.dto.LoginRequest;
import com.nazir.banking.auth.dto.RegisterRequest;
import com.nazir.banking.auth.security.JwtTokenProvider;
import com.nazir.banking.common.exception.BadRequestException;
import com.nazir.banking.user.entity.User;
import com.nazir.banking.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository          userRepository;
    @Mock private PasswordEncoder         passwordEncoder;
    @Mock private JwtTokenProvider        jwtTokenProvider;
    @Mock private AuthenticationManager   authenticationManager;
    @Mock private StringRedisTemplate     redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks private AuthService authService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private void stubRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private void stubTokens() {
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void register_success() {
        stubRedis();
        stubTokens();

        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("Password@1");
        req.setPhone("+1234567890");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        User saved = User.builder()
                .id("id1").firstName("John").lastName("Doe")
                .email("john@example.com").password("hashed")
                .role(User.Role.CUSTOMER).active(true).build();
        when(userRepository.save(any())).thenReturn(saved);

        var response = authService.register(req);

        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void register_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmail("existing@example.com");
        req.setPassword("Password@1");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_success() {
        stubRedis();
        stubTokens();

        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("Password@1");

        User user = User.builder()
                .id("id1").firstName("John").lastName("Doe")
                .email("john@example.com").password("hashed")
                .role(User.Role.CUSTOMER).active(true).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        var response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }
}