package ch.asiankitchen.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.*;

@Component
public class RateLimitFilter implements Filter {
    private record Counter(int count, long windowStartMs) {}
    private final ConcurrentHashMap<String, Counter> buckets = new ConcurrentHashMap<>();
    private final AntPathMatcher m = new AntPathMatcher();

    // 30 requests / 60s per IP per protected path pattern
    private static final int LIMIT = 30;
    private static final long WINDOW_MS = 60_000;
    private static final String[] PROTECTED = {
            "/api/auth/login", "/api/contact/**", "/api/orders/**"
    };

    private boolean matches(HttpServletRequest req) {
        String path = req.getRequestURI();
        for (String p : PROTECTED) if (m.match(p, path)) return true;
        return false;
    }

    @Override public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse res = (HttpServletResponse) sres;

        if (!matches(req)) { chain.doFilter(req, res); return; }

        String ip = req.getRemoteAddr();
        long now = Instant.now().toEpochMilli();

        buckets.compute(ip, (k, v) -> {
            if (v == null || now - v.windowStartMs > WINDOW_MS) return new Counter(1, now);
            return new Counter(v.count + 1, v.windowStartMs);
        });

        Counter c = buckets.get(ip);
        res.setHeader("X-RateLimit-Limit", String.valueOf(LIMIT));
        res.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, LIMIT - c.count)));
        if (c.count > LIMIT) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too many requests. Please try again shortly.\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}
