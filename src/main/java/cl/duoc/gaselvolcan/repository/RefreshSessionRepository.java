package cl.duoc.gaselvolcan.repository;

import cl.duoc.gaselvolcan.model.RefreshSession;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, Long> {
    Optional<RefreshSession> findByTokenHash(String tokenHash);
}
