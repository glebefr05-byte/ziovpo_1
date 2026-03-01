package com.example.rbpo_aeroport.repositories;

import com.example.rbpo_aeroport.entities.ApplicationUser;
import com.example.rbpo_aeroport.entities.UserSession;
import com.example.rbpo_aeroport.models.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findById(UUID id);
    Optional<UserSession> findByRefreshToken(String refreshToken);
    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);
    List<UserSession> findByUserAndStatus(ApplicationUser user, SessionStatus status);
    void deleteByExpiresAtBefore(Instant expiredTime);
}
