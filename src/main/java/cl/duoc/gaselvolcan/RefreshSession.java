package cl.duoc.gaselvolcan;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sesion_refresh")
public class RefreshSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) public UserAccount user;
    @Column(nullable = false, unique = true, length = 64) public String tokenHash;
    @Column(nullable = false) public Instant expiresAt;
    protected RefreshSession() {}
    public RefreshSession(UserAccount user, String tokenHash, Instant expiresAt) {
        this.user = user; this.tokenHash = tokenHash; this.expiresAt = expiresAt;
    }
}
