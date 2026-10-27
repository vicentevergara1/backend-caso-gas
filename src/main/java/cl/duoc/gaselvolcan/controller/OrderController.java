package cl.duoc.gaselvolcan.controller;

import cl.duoc.gaselvolcan.ApiModels;
import cl.duoc.gaselvolcan.services.OrderService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.OrderView place(@Valid @RequestBody ApiModels.OrderRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.place(request, jwt.getSubject());
    }
    @GetMapping public List<ApiModels.OrderView> all() { return service.all(); }
    @GetMapping("/mine") public List<ApiModels.OrderView> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.mine(jwt.getSubject());
    }
    @GetMapping("/{id}") public ApiModels.OrderView one(@PathVariable Long id) { return service.one(id); }
}
