package com.orion.security;

import com.orion.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtEncoder);
    }

    @Test 
    void shouldGenerateToken() {
        User user = new User("test@example.com", "hashed-password");

        Jwt encodedJwt = Jwt.withTokenValue("jwt-token")
            .header("alg", "RS256")
            .claim("sub", user.getId().toString())
            .build();

        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("jwt-token", token);

        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test 
    void shouldIncludeUserIdAsSubject() {

        User user = new User ("test@example.com", "hashed-password");

        Jwt encodedJwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .claim("test", "value")
                .build();
        
        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        jwtService.generateToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertEquals(user.getId().toString(), claims.getSubject());
    }

    @Test 
    void shouldIncludeEmailClaim() {
        User user = new User("test@example.com", "hashed-password");

        Jwt encodedJwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .claim("test", "value")
                .build();
        
        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        jwtService.generateToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertEquals("test@example.com", claims.getClaim("email"));
    }

    @Test
    void shouldSetIssuedAtTime() {
        User user = new User("test@example.com", "hashed-password");

        Jwt encodedJwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .claim("test", "value")
                .build();

        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        Instant before = Instant.now();

        jwtService.generateToken(user);

        Instant after = Instant.now();

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        Instant issuedAt = captor.getValue().getClaims().getIssuedAt();

        assertNotNull(issuedAt);
        assertFalse(issuedAt.isBefore(before));
        assertFalse(issuedAt.isAfter(after));
    }

    @Test 
    void shouldSetExpirationOneHourAfterIssuedAt() {
        User user = new User("test@example.com", "hashed-password");

        Jwt encodedJwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .claim("test", "value")
                .build();

        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        jwtService.generateToken(user);

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        Instant issuedAt = claims.getIssuedAt();
        Instant expiresAt = claims.getExpiresAt();

        assertEquals(60 * 60, expiresAt.getEpochSecond() - issuedAt.getEpochSecond());
    }
}