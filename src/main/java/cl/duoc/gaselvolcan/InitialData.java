package cl.duoc.gaselvolcan;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialData {
    @Bean
    ApplicationRunner seedData(CategoryRepository categories, ProductRepository products,
                               UserRepository users, PasswordEncoder encoder,
                               @Value("${app.admin-email}") String adminEmail,
                               @Value("${app.admin-password}") String adminPassword,
                               @Value("${app.seller-email}") String sellerEmail,
                               @Value("${app.seller-password}") String sellerPassword) {
        return args -> {
            if (categories.count() == 0) categories.saveAll(List.of(
                new Category("Cilindros", "cilindros"), new Category("Mangueras", "mangueras"),
                new Category("Reguladores", "reguladores"), new Category("Accesorios", "accesorios")));
            if (products.count() == 0) {
                Map<String, Category> byName = categories.findAll().stream()
                    .collect(Collectors.toMap(c -> c.name, Function.identity()));
                products.saveAll(List.of(
                    product("cilindro-5kg", "Cilindro Gas 5 Kg", byName.get("Cilindros"), 6500, "5kg.png", "Compacto y liviano, ideal para campings y estufas pequeñas.", false),
                    product("cilindro-11kg", "Cilindro Gas 11 Kg", byName.get("Cilindros"), 12000, "11kg.png", "Formato estándar para cocinas y agua caliente.", false),
                    product("cilindro-15kg", "Cilindro Gas 15 Kg", byName.get("Cilindros"), 16000, "15kg.png", "Mayor rendimiento para consumo familiar.", true),
                    product("cilindro-45kg", "Cilindro Gas 45 Kg", byName.get("Cilindros"), 45000, "45kg.png", "Formato industrial para comercios y empresas.", false),
                    product("manguera-1-5m", "Manguera Gas 1.5 m", byName.get("Mangueras"), 3990, "1.5m.webp", "Manguera flexible para instalaciones domésticas.", true),
                    product("manguera-3m", "Manguera Gas 3 m", byName.get("Mangueras"), 6990, "3m.webp", "Manguera para instalaciones que requieren mayor alcance.", false),
                    product("abrazadera", "Abrazadera Metálica", byName.get("Mangueras"), 990, "abrazadera.webp", "Abrazadera metálica para conexiones.", false),
                    product("kit-conexion", "Kit de Conexión Completo", byName.get("Mangueras"), 12990, "kit.jpeg", "Incluye regulador, manguera y abrazaderas.", false),
                    product("regulador-estandar", "Regulador Estándar", byName.get("Reguladores"), 8990, "regulador-estandar.png", "Regulador de presión para uso doméstico.", true),
                    product("regulador-alta-presion", "Regulador Alta Presión", byName.get("Reguladores"), 18990, "regulador-alta-presion.png", "Regulador de alto flujo para instalaciones compatibles.", false),
                    product("regulador-dual", "Regulador Dual (2 salidas)", byName.get("Reguladores"), 14990, "regulador-dual.png", "Regulador con dos salidas.", false),
                    product("porta-cilindro", "Carro Porta Cilindro", byName.get("Accesorios"), 12990, "porta-cilindro.jpg", "Carro resistente para trasladar cilindros.", false),
                    product("tapa-protectora", "Tapa Protectora", byName.get("Accesorios"), 1490, "tapa-protectora.webp", "Tapa de protección para la válvula.", false),
                    product("detector-fugas", "Detector de Fugas", byName.get("Accesorios"), 19990, "detector.jpg", "Detector de gas con alerta audible.", true)
                ));
            }
            operator(users, encoder, adminEmail, adminPassword, "Administrador", UserAccount.Role.ROLE_ADMIN);
            operator(users, encoder, sellerEmail, sellerPassword, "Vendedor", UserAccount.Role.ROLE_VENDEDOR);
        };
    }

    private static Product product(String id, String name, Category category, int price,
                                   String image, String description, boolean featured) {
        return new Product(id, name, category, price, 100, image, description, featured);
    }
    private static void operator(UserRepository users, PasswordEncoder encoder, String email, String password,
                                 String name, UserAccount.Role role) {
        if (!email.isBlank() && password.length() >= 8 && !users.existsByEmailIgnoreCase(email)) {
            users.save(new UserAccount(name, email.trim().toLowerCase(), encoder.encode(password), role));
        }
    }
}
