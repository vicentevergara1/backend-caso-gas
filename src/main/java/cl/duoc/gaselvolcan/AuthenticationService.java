package cl.duoc.gaselvolcan;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import cl.duoc.gaselvolcan.repository.RefreshSessionRepository;
import cl.duoc.gaselvolcan.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {
    private final UserRepository users;
    private final RefreshSessionRepository refreshSessions;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;
    private final JwtEncoder jwtEncoder;
    private final SecureRandom random = new SecureRandom();

    public record Tokens(ApiModels.AuthResponse response, String refreshToken) {}

    public AuthenticationService(UserRepository users, RefreshSessionRepository refreshSessions,
                                 PasswordEncoder encoder, AuthenticationManager manager, JwtEncoder jwtEncoder) {
        this.users = users; this.refreshSessions = refreshSessions;
        this.encoder = encoder; this.manager = manager; this.jwtEncoder = jwtEncoder;
    }

    @Transactional
    public Tokens register(ApiModels.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        UserAccount user = users.save(new UserAccount(request.name().trim(), email,
            encoder.encode(request.password()), UserAccount.Role.ROLE_CLIENTE));
        return createTokens(user);
    }

    @Transactional
    public Tokens login(ApiModels.LoginRequest request) {
        try {
            manager.authenticate(new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
        } catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        return createTokens(user);
    }

    @Transactional
    public Tokens refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no disponible");
        RefreshSession session = refreshSessions.findByTokenHash(hash(refreshToken))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
        if (!session.expiresAt.isAfter(Instant.now())) {
            refreshSessions.delete(session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        UserAccount user = session.user;
        refreshSessions.delete(session);
        refreshSessions.flush();
        return createTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank())
            refreshSessions.findByTokenHash(hash(refreshToken)).ifPresent(refreshSessions::delete);
    }

    private Tokens createTokens(UserAccount user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("gas-el-volcan")
            .issuedAt(now).expiresAt(now.plus(Duration.ofMinutes(15)))
            .subject(user.email).claim("roles", List.of(user.role.name())).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawRefresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshSessions.save(new RefreshSession(user, hash(rawRefresh), now.plus(Duration.ofDays(7))));
        return new Tokens(new ApiModels.AuthResponse(accessToken, ApiModels.UserView.from(user)), rawRefresh);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
