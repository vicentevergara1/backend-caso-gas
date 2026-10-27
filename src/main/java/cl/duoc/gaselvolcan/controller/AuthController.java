package cl.duoc.gaselvolcan.controller;

import cl.duoc.gaselvolcan.ApiModels;
import cl.duoc.gaselvolcan.AuthenticationService;
import cl.duoc.gaselvolcan.UserAccount;
import cl.duoc.gaselvolcan.repository.UserRepository;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final String COOKIE_NAME = "gas_refresh";
    private final AuthenticationService service;
    private final UserRepository users;
    private final boolean secureCookie;
    private final String sameSite;

    public AuthController(AuthenticationService service, UserRepository users,
                          @Value("${app.cookie-secure}") boolean secureCookie,
                          @Value("${app.cookie-same-site}") String sameSite) {
        this.service = service; this.users = users; this.secureCookie = secureCookie; this.sameSite = sameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiModels.AuthResponse> register(@Valid @RequestBody ApiModels.RegisterRequest request) {
        return tokenResponse(service.register(request), HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<ApiModels.AuthResponse> login(@Valid @RequestBody ApiModels.LoginRequest request) {
        return tokenResponse(service.login(request), HttpStatus.OK);
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiModels.AuthResponse> refresh(@CookieValue(name = COOKIE_NAME, required = false) String refresh) {
        return tokenResponse(service.refresh(refresh), HttpStatus.OK);
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = COOKIE_NAME, required = false) String refresh) {
        service.logout(refresh);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO)).build();
    }
    @GetMapping("/me") @SecurityRequirement(name = "bearerAuth")
    public ApiModels.UserView me(@AuthenticationPrincipal Jwt jwt) {
        UserAccount user = users.findByEmailIgnoreCase(jwt.getSubject()).orElseThrow();
        return ApiModels.UserView.from(user);
    }
    private ResponseEntity<ApiModels.AuthResponse> tokenResponse(AuthenticationService.Tokens tokens, HttpStatus status) {
        return ResponseEntity.status(status).header(HttpHeaders.SET_COOKIE,
            cookie(tokens.refreshToken(), Duration.ofDays(7)))
            .header(HttpHeaders.CACHE_CONTROL, "no-store").body(tokens.response());
    }
    private String cookie(String value, Duration duration) {
        return ResponseCookie.from(COOKIE_NAME, value).httpOnly(true).secure(secureCookie)
            .sameSite(sameSite).path("/api/v1/auth").maxAge(duration).build().toString();
    }
}
