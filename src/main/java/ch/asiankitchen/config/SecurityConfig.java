package ch.asiankitchen.config;

import ch.asiankitchen.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

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

    @Value("${app.cors.allowed-origins:*}")
    private String[] allowedOrigins;

    @Value("${app.security.auth-cookie-name:AK_AUTH}")
    private String authCookieName;

    @Value("${app.security.cookie-domain:}") // e.g. .asian-kitchen.online in prod
    private String cookieDomain;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(
                "https://asian-kitchen.online",
                "https://www.asian-kitchen.online"
        ));
        cors.setAllowCredentials(true);
        cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of(
                "Content-Type","Accept","X-Requested-With","X-XSRF-TOKEN","Authorization"
        ));
        cors.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   HandlerMappingIntrospector introspector) throws Exception {
        var cookieJwtFilter = new JwtCookieAuthFilter(jwtTokenProvider, userDetailsService, authCookieName);

        // build once
        var csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepo.setCookieCustomizer(c -> c
                .domain(".asian-kitchen.online") // <-- share with apex + subdomains
                .path("/")
                .sameSite("Lax")
                .secure(true)
        );

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepo)
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/orders", "POST"),
                                new AntPathRequestMatcher("/api/payments/**")
                        )
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(daoAuthProvider())
                .addFilterBefore(cookieJwtFilter, UsernamePasswordAuthenticationFilter.class) // <-- keep only once
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/food-items/**","/api/menu-items/**","/api/buffet-items/**","/api/restaurant-info/**"
                        ).permitAll()
                        .requestMatchers("/api/contact/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitRegistration(RateLimitFilter f) {
        var reg = new FilterRegistrationBean<>(f);
        // run BEFORE Spring Security (-100). Any number < -100 is fine:
        reg.setOrder(-110);
        reg.addUrlPatterns(
                "/api/auth/login",
                "/api/contact/*",
                "/api/reservations",
                "/api/orders"
        );
        // optionally: exclude health if your filter supports it, or don’t include it above
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
                var cookies = req.getCookies();
                if (cookies != null) {
                    for (Cookie c : cookies) if (cookieName.equals(c.getName())) { token = c.getValue(); break; }
                }
                if (token != null && jwt.validateToken(token)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var user = users.loadUserByUsername(jwt.getUsername(token));
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) { /* continue unauthenticated */ }

            chain.doFilter(req, res);
        }
    }
}
