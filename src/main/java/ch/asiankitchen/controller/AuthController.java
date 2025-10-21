package ch.asiankitchen.controller;

import ch.asiankitchen.config.JwtTokenProvider;
import ch.asiankitchen.dto.AuthRequestDTO;
import ch.asiankitchen.dto.RegisterRequestDTO;
import ch.asiankitchen.dto.UserReadDTO;
import ch.asiankitchen.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    // cookie props
    @Value("${app.security.auth-cookie-name:AK_AUTH}")
    private String authCookieName;

    @Value("${app.security.cookie-domain:}")
    private String cookieDomain;

    @Value("${app.security.cookie-secure:true}")
    private boolean cookieSecure;

    @Value("${app.security.same-site:None}")
    private String sameSite;

    // NEW: role-based lifetimes (seconds)
    @Value("${jwt.user-validity-seconds:604800}")
    private long userValiditySeconds;

    @Value("${jwt.admin-validity-seconds:2592000}")
    private long adminValiditySeconds;

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

            // create JWT with role-based expiry
            String token = jwtTokenProvider.createToken(req.getUsername(), role);

            boolean isAdmin = role.contains("ADMIN");
            long maxAgeSeconds = isAdmin ? adminValiditySeconds : userValiditySeconds;

            var cookie = buildAuthCookie(token, maxAgeSeconds);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(Map.of(
                            "username", req.getUsername(),
                            "role", role,
                            // keep key for backward compat; value now reflects role-based TTL
                            "expiresInMs", maxAgeSeconds * 1000L
                    ));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        var cookie = clearAuthCookie();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().headers(headers).body(Map.of("status", "ok"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String role = user.getAuthorities().stream()
                .findFirst().map(GrantedAuthority::getAuthority).orElse(null);
        return ResponseEntity.ok(Map.of("username", user.getUsername(), "role", role));
    }

    // --- cookie builders ---
    private ResponseCookie buildAuthCookie(String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(authCookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
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
