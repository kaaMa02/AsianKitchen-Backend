package ch.asiankitchen.controller;

import ch.asiankitchen.config.JwtTokenProvider;
import ch.asiankitchen.dto.AuthRequestDTO;
import ch.asiankitchen.dto.RegisterRequestDTO;
import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
    }

    // === Configurable cookie props ===
    @Value("${app.security.auth-cookie-name:AK_AUTH}")
    private String authCookieName;

    // e.g. ".asian-kitchen.online" in prod; leave empty on localhost
    @Value("${app.security.cookie-domain:}")
    private String cookieDomain;

    // true on prod (https), false locally
    @Value("${app.security.cookie-secure:true}")
    private boolean cookieSecure;

    // "None" on prod (cross-site), "Lax" locally
    @Value("${app.security.same-site:None}")
    private String sameSite;

    // JWT TTL
    @Value("${jwt.expiration-ms:3600000}")
    private long jwtTtlMs;

    @PostMapping("/register")
    public ResponseEntity<UserReadDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        UserReadDTO created = authService.register(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequestDTO req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            String role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_CUSTOMER");

            String token = jwtTokenProvider.createToken(req.getUsername(), role);

            // Set HttpOnly auth cookie
            var cookie = buildAuthCookie(token, jwtTtlMs);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

            // You can return minimal info for the FE
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(Map.of(
                            "username", req.getUsername(),
                            "role", role,
                            "expiresInMs", jwtTtlMs
                    ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Clear the cookie
        var cookie = clearAuthCookie();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().headers(headers).body(Map.of("status", "ok"));
    }

    // Optional helper so FE can fetch current user/role
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String role = user.getAuthorities().stream()
                .findFirst().map(GrantedAuthority::getAuthority).orElse(null);
        return ResponseEntity.ok(Map.of("username", user.getUsername(), "role", role));
    }

    // -------- cookie builders --------

    private ResponseCookie buildAuthCookie(String value, long ttlMs) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(authCookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofMillis(ttlMs))
                .sameSite(sameSite);
        if (StringUtils.hasText(cookieDomain)) {
            b.domain(cookieDomain);
        }
        return b.build();
    }

    private ResponseCookie clearAuthCookie() {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(authCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite(sameSite);
        if (StringUtils.hasText(cookieDomain)) {
            b.domain(cookieDomain);
        }
        return b.build();
    }
}