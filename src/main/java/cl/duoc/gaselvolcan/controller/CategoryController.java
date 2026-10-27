package cl.duoc.gaselvolcan.controller;

import cl.duoc.gaselvolcan.dto.ApiModels;
import cl.duoc.gaselvolcan.repository.CategoryRepository;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryRepository categories;
    public CategoryController(CategoryRepository categories) { this.categories = categories; }
    @GetMapping public List<ApiModels.CategoryView> all() {
        return categories.findAll().stream().map(c -> new ApiModels.CategoryView(c.id, c.name, c.slug)).toList();
    }
}
