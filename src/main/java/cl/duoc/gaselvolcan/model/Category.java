package cl.duoc.gaselvolcan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true, length = 80) public String name;
    @Column(nullable = false, unique = true, length = 80) public String slug;
    protected Category() {}
    public Category(String name, String slug) { this.name = name; this.slug = slug; }
}
