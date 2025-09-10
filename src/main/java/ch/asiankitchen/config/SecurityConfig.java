package ch.asiankitchen.config;

import ch.asiankitchen.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // --- cookie / cors settings from application.properties ---
    @Value("${app.security.auth-cookie-name:AK_AUTH}")
    private String authCookieName;

    @Value("${app.security.cookie-domain:}")
    private String cookieDomain;              // ".asian-kitchen.online" in prod, empty locally

    @Value("${app.security.cookie-secure:true}")
    private boolean cookieSecure;             // true in prod (HTTPS), false locally

    @Value("${app.security.same-site:None}")
    private String sameSite;                  // "None" in prod, "Lax" locally if you want

    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsCsv;         // comma-separated list of FE origins

    // --- auth manager / encoder ---
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- CORS ---
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();

        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(origins); // exact origins
        cors.setAllowCredentials(true);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Content-Type", "Accept", "X-Requested-With", "X-XSRF-TOKEN", "Authorization"));
        // cors.setExposedHeaders(List.of("Set-Cookie")); // rarely needed
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    // --- Security filter chain ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var cookieJwtFilter = new JwtCookieAuthFilter(jwtTokenProvider, userDetailsService, authCookieName);

        var csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepo.setCookieCustomizer(b -> {
            b.path("/")
                    .sameSite(sameSite)
                    .secure(cookieSecure);
            if (StringUtils.hasText(cookieDomain)) {
                b.domain(cookieDomain);
            }
        });

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepo)
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/auth/**"),
                                new AntPathRequestMatcher("/api/contact"),
                                new AntPathRequestMatcher("/api/orders", "POST"),
                                new AntPathRequestMatcher("/api/reservations", "POST"),
                                new AntPathRequestMatcher("/api/payments/**")
                        )
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(daoAuthProvider())
                .addFilterBefore(cookieJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/food-items/**",
                                "/api/menu-items/**",
                                "/api/buffet-items/**",
                                "/api/restaurant-info/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                        .requestMatchers("/api/contact/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/*/track").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/*/track").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    // If you use the provided RateLimitFilter
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitRegistration(RateLimitFilter f) {
        var reg = new FilterRegistrationBean<>(f);
        reg.setOrder(-110); // before Spring Security (-100)
        reg.addUrlPatterns("/api/auth/login", "/api/contact/*", "/api/reservations", "/api/orders");
        return reg;
    }

    /**
     * Reads JWT from HttpOnly cookie and populates SecurityContext if valid.
     */
    static class JwtCookieAuthFilter extends org.springframework.web.filter.OncePerRequestFilter {
        private final JwtTokenProvider jwt;
        private final CustomUserDetailsService users;
        private final String cookieName;

        JwtCookieAuthFilter(JwtTokenProvider jwt,
                            CustomUserDetailsService users,
                            String cookieName) {
            this.jwt = jwt;
            this.users = users;
            this.cookieName = cookieName;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {

            try {
                String token = null;
                Cookie[] cookies = req.getCookies();
                if (cookies != null) {
                    for (Cookie c : cookies) {
                        if (cookieName.equals(c.getName())) {
                            token = c.getValue();
                            break;
                        }
                    }
                }

                if (token != null && jwt.validateToken(token)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    var user = users.loadUserByUsername(jwt.getUsername(token));
                    var auth = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignore) {
                // continue unauthenticated
            }

            chain.doFilter(req, res);
        }
    }
}
