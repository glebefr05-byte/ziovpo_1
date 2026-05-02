package com.licensing.repository;

import com.licensing.entities.User;
import com.licensing.entities.UserSession;
import com.licensing.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findById(UUID id);
    Optional<UserSession> findByRefreshToken(String refreshToken);
    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);
    List<UserSession> findByUserAndStatus(User user, SessionStatus status);
    void deleteByExpiresAtBefore(Instant expiredTime);
}
