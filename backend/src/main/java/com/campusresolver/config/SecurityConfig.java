package com.campusresolver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration
 *
 * Security rules:
 * - /api/complaints/** → Open (students don't need to log in for MVP)
 * - /api/students/** → Open
 * - /api/departments/** → Open for GET, protected for POST
 * - /api/admin/** → Requires ADMIN role (Basic Auth)
 *
 * For production: replace with JWT token authentication.
 * For the internship demo: Basic Auth is sufficient.
 *
 * Admin credentials: admin / admin123
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF for REST API (stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/complaints/**").permitAll()
                        .requestMatchers("/api/students/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/departments/**").permitAll()

                        // Admin endpoints require authentication
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("ADMIN")

                        // Everything else is public (returns 404 instead of Basic Auth popup)
                        .anyRequest().permitAll())

                // Enable Basic Authentication
                .httpBasic(basic -> {
                });

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.Arrays.asList("*")); // Allow all for MVP
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(false); // Can't be true if allowedOrigins is *
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // In-memory admin user for demo purposes
        // In production: load users from database
        var adminUser = User.builder()
                .username("bitm")
                .password(passwordEncoder().encode("bitm@123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(adminUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
