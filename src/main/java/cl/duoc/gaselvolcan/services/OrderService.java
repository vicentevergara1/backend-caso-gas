package cl.duoc.gaselvolcan.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cl.duoc.gaselvolcan.ApiModels;
import cl.duoc.gaselvolcan.OrderEntity;
import cl.duoc.gaselvolcan.OrderLine;
import cl.duoc.gaselvolcan.UserAccount;
import cl.duoc.gaselvolcan.model.Product;
import cl.duoc.gaselvolcan.repository.OrderRepository;
import cl.duoc.gaselvolcan.repository.ProductRepository;
import cl.duoc.gaselvolcan.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders; this.products = products; this.users = users;
    }

    @Transactional
    public ApiModels.OrderView place(ApiModels.OrderRequest request, String email) {
        UserAccount account = findUser(email);
        OrderEntity order = new OrderEntity();
        order.user = account;
        order.customerName = request.name().trim(); order.phone = request.phone().trim();
        order.email = request.email().trim(); order.address = request.address().trim();
        order.commune = request.commune().trim(); order.paymentMethod = request.paymentMethod();
        if (request.items().size() > 50)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Demasiados productos en el pedido");
        Set<String> used = new HashSet<>();
        int total = 0;
        for (ApiModels.CartLine line : request.items()) {
            if (!used.add(line.id()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto repetido en el pedido");
            Product product = products.lockById(line.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no disponible: " + line.id()));
            if (product.stock < line.qty())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente de " + product.name);
            product.stock -= line.qty();
            try { total = Math.addExact(total, Math.multiplyExact(product.price, line.qty())); }
            catch (ArithmeticException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total de compra inválido");
            }
            order.items.add(new OrderLine(order, product, line.qty()));
        }
        order.total = total;
        return ApiModels.OrderView.from(orders.saveAndFlush(order));
    }

    @Transactional(readOnly = true)
    public List<ApiModels.OrderView> all() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(ApiModels.OrderView::from).toList();
    }
    @Transactional(readOnly = true)
    public ApiModels.OrderView one(Long id) {
        return ApiModels.OrderView.from(orders.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido inexistente")));
    }
    @Transactional(readOnly = true)
    public List<ApiModels.OrderView> mine(String email) {
        return orders.findByUserIdOrderByCreatedAtDesc(findUser(email).id).stream()
            .map(ApiModels.OrderView::from).toList();
    }
    private UserAccount findUser(String email) {
        return users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
    }
}
