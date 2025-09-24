package ch.asiankitchen.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CsrfController {

    private final CookieCsrfTokenRepository repo;

    public CsrfController() {
        // same settings as SecurityConfig (readable cookie)
        this.repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @GetMapping("/api/csrf")
    public ResponseEntity<Map<String, String>> csrf(HttpServletRequest req) {
        // Try to read the token put by CsrfFilter (if present)
        CsrfToken token = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            // Generate one ourselves and make the repo add the cookie
            token = repo.generateToken(req);
            repo.saveToken(token, req, null); // response is null here, so just return value below
        }
        return ResponseEntity.ok(Map.of(
                "headerName", token.getHeaderName(),
                "paramName", token.getParameterName(),
                "token", token.getToken()
        ));
    }
}
