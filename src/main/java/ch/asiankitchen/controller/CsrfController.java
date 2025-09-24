package ch.asiankitchen.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CsrfController {

    private final CookieCsrfTokenRepository repo;

    public CsrfController(CookieCsrfTokenRepository repo) {
        this.repo = repo; // use the same cookie settings as SecurityConfig
    }

    @GetMapping("/api/csrf")
    public ResponseEntity<Map<String, String>> csrf(HttpServletRequest req, HttpServletResponse res) {
        // If CsrfFilter already created a token, it will be here and the repo will write the cookie.
        CsrfToken token = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            token = repo.generateToken(req);
        }
        repo.saveToken(token, req, res);

        return ResponseEntity.ok(Map.of(
                "headerName", token.getHeaderName(),
                "paramName",  token.getParameterName(),
                "token",      token.getToken()
        ));
    }
}
