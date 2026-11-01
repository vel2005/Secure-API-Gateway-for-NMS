package com.example.secure_api_gateway.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

@Configuration
public class AuthorizationServerConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        // Temporary user for development

        // Register OAuth2 client
        @Bean
        public RegisteredClientRepository registeredClientRepository(
                        PasswordEncoder passwordEncoder) {

                RegisteredClient client = RegisteredClient
                                .withId(UUID.randomUUID().toString())
                                .clientId("nms-postman-client")
                                .clientSecret(passwordEncoder.encode("nms-secret"))
                                .clientAuthenticationMethod(
                                                ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(
                                                AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(
                                                AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri("https://oauth.pstmn.io/v1/callback")
                                .scope(OidcScopes.OPENID)
                                .scope(OidcScopes.PROFILE)
                                .scope("devices.read")
                                .scope("devices.write")
                                .scope("alarms.read")
                                .scope("alarms.write")
                                .clientSettings(
                                                ClientSettings.builder()
                                                                .requireProofKey(true)
                                                                .requireAuthorizationConsent(false)
                                                                .build())
                                .tokenSettings(
                                                TokenSettings.builder()
                                                                .accessTokenTimeToLive(
                                                                                Duration.ofMinutes(15))
                                                                .refreshTokenTimeToLive(
                                                                                Duration.ofHours(8))
                                                                .build())
                                .build();

                return new InMemoryRegisteredClientRepository(client);
        }

        // Generate RSA keys for JWT signing
        @Bean
        public JWKSource<SecurityContext> jwkSource() {

                KeyPair keyPair = generateRsaKey();

                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

                RSAKey rsaKey = new RSAKey.Builder(publicKey)
                                .privateKey(privateKey)
                                .keyID(UUID.randomUUID().toString())
                                .build();

                JWKSet jwkSet = new JWKSet(rsaKey);

                return new ImmutableJWKSet<>(jwkSet);
        }

        private static KeyPair generateRsaKey() {

                try {
                        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

                        generator.initialize(2048);

                        return generator.generateKeyPair();

                } catch (Exception exception) {
                        throw new IllegalStateException(
                                        "Unable to generate RSA keys", exception);
                }
        }

        // Decode JWT tokens
        @Bean
        public JwtDecoder jwtDecoder(
                        JWKSource<SecurityContext> jwkSource) {

                return OAuth2AuthorizationServerConfiguration
                                .jwtDecoder(jwkSource);
        }

        // Customize JWT access tokens to include user roles for RBAC
        @Bean
        public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
                return context -> {
                        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                                context.getClaims().claims(claims -> {
                                        Set<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities());
                                        claims.put("roles", roles);
                                });
                        }
                };
        }

        // Authorization server settings
        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {

                return AuthorizationServerSettings.builder()
                                .issuer("http://localhost:8080")
                                .build();
        }
}
