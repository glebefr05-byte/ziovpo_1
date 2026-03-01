package com.example.rbpo_aeroport.services;

import com.example.rbpo_aeroport.configuration.JwtTokenProvider;
import com.example.rbpo_aeroport.entities.ApplicationUser;
import com.example.rbpo_aeroport.entities.UserSession;
import com.example.rbpo_aeroport.models.AuthenticationResponse;
import com.example.rbpo_aeroport.models.enums.SessionStatus;
import com.example.rbpo_aeroport.repositories.ApplicationUserRepository;
import com.example.rbpo_aeroport.repositories.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtTokenProvider tokenProvider;
    private final UserSessionRepository sessionRepository;
    private final ApplicationUserRepository applicationUserRepository;

    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;

    // Первая генерация пары токенов
    @Transactional
    public AuthenticationResponse generateTokenPair(Authentication authentication, String deviceId) {
        String email = authentication.getName();

        ApplicationUser user = applicationUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("The user with this email was not found."));

        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceId(deviceId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        session.setRefreshToken("temp"); // временное значение
        session = sessionRepository.save(session);

        String accessToken = tokenProvider.createAccessToken(
                email,
                user.getRole().getGrantedAuthorities(),
                session.getId().toString()
        );
        String refreshToken = tokenProvider.createRefreshToken(email, session.getId().toString());
        session.setRefreshToken(refreshToken);
        sessionRepository.save(session);
        return new AuthenticationResponse(email, accessToken, refreshToken);
    }

    // Обновление пары токенов по refreshToken
    @Transactional
    public AuthenticationResponse refreshTokens(String refreshToken) {

        String sessionId = tokenProvider.getSessionIdFromToken(refreshToken);
        UserSession session = sessionRepository.findById(UUID.fromString(sessionId))
                .orElseThrow(() -> new SecurityException("Session not found"));
        if (!tokenProvider.validateToken(refreshToken) ||
                !tokenProvider.validateTokenType(refreshToken, "REFRESH")) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);
            throw new SecurityException("Invalid refresh token");
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);
            throw new SecurityException("Session is not active");
        }
        if (!session.getRefreshToken().equals(refreshToken)) {
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);
            throw new SecurityException("Refresh token mismatch");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new SecurityException("Session expired");
        }

        session.setStatus(SessionStatus.USED);
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);

        ApplicationUser user = session.getUser();
        if (user == null) {
            throw new SecurityException("User not found in session");
        }

        UserSession newSession = new UserSession();
        newSession.setUser(user);
        newSession.setDeviceId(session.getDeviceId());
        newSession.setStatus(SessionStatus.ACTIVE);
        newSession.setCreatedAt(Instant.now());
        newSession.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        newSession.setRefreshToken("temp");
        newSession = sessionRepository.save(newSession);


        String newAccessToken = tokenProvider.createAccessToken(
                user.getEmail(),
                user.getRole().getGrantedAuthorities(),
                newSession.getId().toString()
        );
        String newRefreshToken = tokenProvider.createRefreshToken(user.getEmail(), newSession.getId().toString());

        newSession.setRefreshToken(newRefreshToken);
        sessionRepository.save(newSession);

        return new AuthenticationResponse(user.getEmail(), newAccessToken, newRefreshToken);
    }
}