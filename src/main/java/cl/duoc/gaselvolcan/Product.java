package cl.duoc.gaselvolcan;

import jakarta.persistence.*;

@Entity
@Table(name = "producto")
public class Product {
    @Id @Column(length = 90) public String id;
    @Column(nullable = false, length = 130) public String name;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "categoria_id") public Category category;
    @Column(nullable = false) public int price;
    @Column(nullable = false) public int stock;
    @Lob @Column(nullable = false, columnDefinition = "LONGTEXT") public String image;
    @Column(nullable = false, length = 2000) public String description;
    @Column(nullable = false) public boolean featured;
    protected Product() {}
    public Product(String id, String name, Category category, int price, int stock, String image, String description, boolean featured) {
        this.id = id; this.name = name; this.category = category; this.price = price; this.stock = stock;
        this.image = image; this.description = description; this.featured = featured;
    }
}
