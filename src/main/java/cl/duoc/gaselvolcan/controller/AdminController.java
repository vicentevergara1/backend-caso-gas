package cl.duoc.gaselvolcan.controller;

import cl.duoc.gaselvolcan.dto.ApiModels;
import cl.duoc.gaselvolcan.repository.UserRepository;

import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final UserRepository users;
    public AdminController(UserRepository users) { this.users = users; }
    @GetMapping public List<ApiModels.UserView> all() {
        return users.findAll().stream().map(ApiModels.UserView::from).toList();
    }
}
