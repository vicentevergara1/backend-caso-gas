package cl.duoc.gaselvolcan;

import cl.duoc.gaselvolcan.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;

public final class ApiModels {
    private ApiModels() {}

    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email,
                                  @NotBlank @Size(min = 8, max = 72) String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record UserView(Long id, String name, String email, String role) {
        public static UserView from(UserAccount user) {
            return new UserView(user.id, user.name, user.email, user.role.name().replace("ROLE_", ""));
        }
    }
    public record AuthResponse(String accessToken, UserView user) {}
    public record ProductRequest(@NotBlank String name, @NotBlank String category,
                                 @NotNull @Min(1) Integer price, @NotBlank String image,
                                 @NotBlank String description, Boolean featured,
                                 @NotNull @Min(0) Integer stock) {}
    public record ProductView(String id, String name, String category, String categoryAnchor,
                              int price, int stock, String image, String description, boolean featured) {
        public static ProductView from(Product product) {
            return new ProductView(product.id, product.name, product.category.name,
                product.category.slug, product.price, product.stock, product.image,
                product.description, product.featured);
        }
    }
    public record CartLine(@NotBlank String id, @NotNull @Min(1) @Max(50) Integer qty) {}
    public record OrderRequest(@NotEmpty List<@Valid CartLine> items,
                               @NotBlank String name, @NotBlank String phone,
                               @Email @NotBlank String email, @NotBlank String address,
                               @NotBlank String commune,
                               @NotBlank @Pattern(regexp = "card|transfer|cash") String paymentMethod) {}
    public record OrderLineView(String productId, String productName, int unitPrice, int quantity) {}
    public record OrderView(Long id, String customerName, String email, String address,
                            String commune, String phone, String paymentMethod, String status,
                            int total, Instant createdAt, List<OrderLineView> items) {
        public static OrderView from(OrderEntity order) {
            return new OrderView(order.id, order.customerName, order.email, order.address,
                order.commune, order.phone, order.paymentMethod, order.status,
                order.total, order.createdAt, order.items.stream()
                    .map(item -> new OrderLineView(item.productId, item.productName,
                        item.unitPrice, item.quantity)).toList());
        }
    }
    public record ErrorResponse(String error) {}
}
