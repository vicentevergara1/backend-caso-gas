package cl.duoc.gaselvolcan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class UserAccount {
    public enum Role { ROLE_ADMIN, ROLE_VENDEDOR, ROLE_CLIENTE }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, length = 120) public String name;
    @Column(nullable = false, unique = true, length = 180) public String email;
    @Column(nullable = false) public String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25) public Role role;
    protected UserAccount() {}
    public UserAccount(String name, String email, String passwordHash, Role role) {
        this.name = name; this.email = email; this.passwordHash = passwordHash; this.role = role;
    }
}
