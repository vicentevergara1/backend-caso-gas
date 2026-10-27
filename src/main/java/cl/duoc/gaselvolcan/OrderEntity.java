package cl.duoc.gaselvolcan;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boleta")
public class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id") public UserAccount user;
    @Column(nullable = false) public Instant createdAt = Instant.now();
    @Column(nullable = false) public String customerName;
    @Column(nullable = false) public String phone;
    @Column(nullable = false) public String email;
    @Column(nullable = false) public String address;
    @Column(nullable = false) public String commune;
    @Column(nullable = false) public String paymentMethod;
    @Column(nullable = false) public String status = "PENDIENTE";
    @Column(nullable = false) public int total;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderLine> items = new ArrayList<>();
    protected OrderEntity() {}
}
