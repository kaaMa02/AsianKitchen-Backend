package ch.asiankitchen.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CsrfController {
    @GetMapping("/api/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        // Returning the token forces Spring to create the XSRF-TOKEN cookie.
        return Map.of("token", token.getToken());
    }
}
