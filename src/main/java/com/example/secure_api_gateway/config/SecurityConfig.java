package com.example.secure_api_gateway.config;

import com.example.secure_api_gateway.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;

        public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
                this.customUserDetailsService = customUserDetailsService;
        }

        // First filter chain: OAuth2 Authorization Server
        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(
                        HttpSecurity http) throws Exception {

                http.cors(Customizer.withDefaults());

                http.oauth2AuthorizationServer(authorizationServer -> {
                        http.securityMatcher(
                                        authorizationServer.getEndpointsMatcher());

                        authorizationServer.oidc(Customizer.withDefaults());
                });

                http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

                http.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

                return http.build();
        }

        // Second filter chain: Application Security & RBAC
        @Bean
        @Order(2)
        public SecurityFilterChain applicationSecurityFilterChain(
                        HttpSecurity http) throws Exception {

                http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .userDetailsService(customUserDetailsService)
                                .authorizeHttpRequests(auth -> auth

                                                // 1. Allow public user registration
                                                .requestMatchers(HttpMethod.POST, "/api/users", "/api/users/register").permitAll()

                                                // 2. Restrict all other user management to ADMIN
                                                .requestMatchers("/api/users/**")
                                                .hasRole("ADMIN")

                                                // Device read operations
                                                .requestMatchers(HttpMethod.GET, "/api/devices/**")
                                                .hasAnyRole("ADMIN", "ENGINEER", "VIEWER")

                                                // Device modification operations
                                                .requestMatchers("/api/devices/**")
                                                .hasAnyRole("ADMIN", "ENGINEER")

                                                // Alarm read operations
                                                .requestMatchers(HttpMethod.GET, "/api/alarms/**")
                                                .hasAnyRole("ADMIN", "ENGINEER", "VIEWER")

                                                // Alarm modification operations
                                                .requestMatchers("/api/alarms/**")
                                                .hasAnyRole("ADMIN", "ENGINEER")

                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                                .formLogin(Customizer.withDefaults())
                                .httpBasic(Customizer.withDefaults());

                return http.build();
        }

        // Extracts custom "roles" claim from JWT and maps to Spring Security GrantedAuthorities
        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
                grantedAuthoritiesConverter.setAuthorityPrefix("");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return jwtAuthenticationConverter;
        }

        // Global CORS Configuration for React Frontend
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://localhost:8000"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
                configuration.setExposedHeaders(List.of("Authorization", "X-RateLimit-Limit-Minute", "X-RateLimit-Remaining-Minute"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
