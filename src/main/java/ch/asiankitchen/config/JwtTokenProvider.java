package ch.asiankitchen.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long userValiditySeconds;
    private final long adminValiditySeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.user-validity-seconds:604800}") long userValiditySeconds,      // 7 days
            @Value("${jwt.admin-validity-seconds:2592000}") long adminValiditySeconds   // 30 days
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.userValiditySeconds = userValiditySeconds;
        this.adminValiditySeconds = adminValiditySeconds;
    }

    /** role is like "ROLE_ADMIN" or "ROLE_CUSTOMER" */
    public String createToken(String username, String role) {
        boolean isAdmin = role != null && role.contains("ADMIN");
        long validityMs = (isAdmin ? adminValiditySeconds : userValiditySeconds) * 1000L;

        Date now = new Date();
        Date expires = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // e.g. "ROLE_ADMIN"
                .setIssuedAt(now)
                .setExpiration(expires)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRole(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object r = claims.get("role");
        return r != null ? r.toString() : null;
    }
}