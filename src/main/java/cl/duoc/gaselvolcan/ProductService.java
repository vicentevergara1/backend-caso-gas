package cl.duoc.gaselvolcan;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products; this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<ApiModels.ProductView> all() {
        return products.findAll().stream().map(ApiModels.ProductView::from).toList();
    }

    @Transactional(readOnly = true)
    public ApiModels.ProductView one(String id) {
        return ApiModels.ProductView.from(products.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente")));
    }

    @Transactional
    public ApiModels.ProductView create(ApiModels.ProductRequest request) {
        Product product = new Product("producto-" + UUID.randomUUID(), "", category(request.category()),
            1, 0, "", "", false);
        fill(product, request);
        return ApiModels.ProductView.from(products.save(product));
    }

    @Transactional
    public ApiModels.ProductView update(String id, ApiModels.ProductRequest request) {
        Product product = products.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente"));
        fill(product, request);
        return ApiModels.ProductView.from(products.save(product));
    }

    @Transactional
    public void delete(String id) {
        if (!products.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto inexistente");
        products.deleteById(id);
    }

    private Category category(String name) {
        return categories.findByName(name)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría no válida"));
    }

    private void fill(Product product, ApiModels.ProductRequest input) {
        String image = input.image().trim();
        boolean staticImage = image.matches("[A-Za-z0-9._-]{1,100}\\.(png|jpe?g|webp|avif)");
        boolean uploadedImage = image.matches("(?s)data:image/(jpeg|png|webp);base64,[A-Za-z0-9+/=]{1,280000}");
        if (!staticImage && !uploadedImage)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagen inválida o demasiado grande");
        if (input.name().trim().length() > 130 || input.description().trim().length() > 2000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Texto demasiado largo");
        product.name = input.name().trim();
        product.category = category(input.category());
        product.price = input.price();
        product.stock = input.stock();
        product.image = image;
        product.description = input.description().trim();
        product.featured = Boolean.TRUE.equals(input.featured());
    }
}
