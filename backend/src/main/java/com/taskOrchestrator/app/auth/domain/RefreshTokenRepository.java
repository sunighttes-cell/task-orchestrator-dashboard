package com.taskOrchestrator.app.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByRevokedAtIsNull();
    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);
    List<RefreshToken> findByRevokedAtIsNotNull();
    List<RefreshToken> findByUser(User user);
}