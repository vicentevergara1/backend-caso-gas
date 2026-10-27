package cl.duoc.gaselvolcan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_boleta")
public class OrderLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "boleta_id") public OrderEntity order;
    @Column(nullable = false, length = 90) public String productId;
    @Column(nullable = false) public String productName;
    @Column(nullable = false) public int unitPrice;
    @Column(nullable = false) public int quantity;
    protected OrderLine() {}
    public OrderLine(OrderEntity order, Product product, int quantity) {
        this.order = order; this.productId = product.id; this.productName = product.name;
        this.unitPrice = product.price; this.quantity = quantity;
    }
}
