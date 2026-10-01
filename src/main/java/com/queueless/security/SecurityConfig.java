package com.queueless.security;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final List<String> allowedOrigins;

        public SecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        @Value("${app.cors.allowed-origins}") String allowedOrigins) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isEmpty())
                                .toList();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // Authentication
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/register")
                                                .permitAll()

                                                // CUSTOMER
                                                .requestMatchers(
                                                                "/api/tickets/join",
                                                                "/api/tickets/*/cancel",
                                                                "/api/tickets/*/position",
                                                                "/api/tickets/*/estimated-wait",
                                                                "/api/tickets/*/status")
                                                .hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                                                // STAFF + ADMIN
                                                .requestMatchers(
                                                                "/api/tickets/queue/*/call-next",
                                                                "/api/tickets/*/complete")
                                                .hasAnyRole("STAFF", "ADMIN")

                                                // Read-only queue choices for joining a service queue
                                                .requestMatchers(HttpMethod.GET, "/api/queues/options")
                                                .hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                                                // Queue management
                                                .requestMatchers(
                                                                "/api/queues/**")
                                                .hasAnyRole("STAFF", "ADMIN")

                                                // Service management
                                                // View services
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/services/**")
                                                .hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                                                // Create services
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/services/**")
                                                .hasRole("ADMIN")

                                                // Delete services
                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/services/**")
                                                .hasRole("ADMIN")

                                                // User management
                                                .requestMatchers(
                                                                "/api/users/**")
                                                .hasRole("ADMIN")

                                                // Everything else requires login
                                                .anyRequest().authenticated())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(allowedOrigins);

                configuration.setAllowedMethods(
                                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of("*"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
