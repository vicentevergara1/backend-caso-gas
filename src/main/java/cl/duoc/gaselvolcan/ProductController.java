package cl.duoc.gaselvolcan;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @GetMapping public List<ApiModels.ProductView> all() { return service.all(); }
    @GetMapping("/{id}") public ApiModels.ProductView one(@PathVariable String id) { return service.one(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @SecurityRequirement(name = "bearerAuth")
    public ApiModels.ProductView create(@Valid @RequestBody ApiModels.ProductRequest request) { return service.create(request); }
    @PutMapping("/{id}") @SecurityRequirement(name = "bearerAuth")
    public ApiModels.ProductView update(@PathVariable String id, @Valid @RequestBody ApiModels.ProductRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @SecurityRequirement(name = "bearerAuth")
    public void delete(@PathVariable String id) { service.delete(id); }
}
