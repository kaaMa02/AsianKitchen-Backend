package ch.asiankitchen.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.expiration-ms}") long validityInMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.validityInMs = validityInMs;
    }

    public String createToken(String username, String role) {
        Date now = new Date();
        Date expires = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // e.g. "ROLE_ADMIN" / "ROLE_CUSTOMER"
                .setIssuedAt(now)
                .setExpiration(expires)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
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

    /** Returns the "role" claim as stored (e.g. "ROLE_ADMIN"). */
    public String getRole(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object r = claims.get("role");
        return (r != null) ? r.toString() : null;
    }
}
