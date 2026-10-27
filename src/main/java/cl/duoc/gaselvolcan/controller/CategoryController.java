package cl.duoc.gaselvolcan.controller;

import java.util.List;

import cl.duoc.gaselvolcan.repository.CategoryRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryRepository categories;
    public CategoryController(CategoryRepository categories) { this.categories = categories; }
    public record CategoryView(Long id, String name, String slug) {}
    @GetMapping public List<CategoryView> all() {
        return categories.findAll().stream().map(c -> new CategoryView(c.id, c.name, c.slug)).toList();
    }
}
