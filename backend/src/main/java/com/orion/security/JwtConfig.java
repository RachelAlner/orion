package com.orion.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {
    // authorise 
    @Value("classpath:keys/private.pem")
    private Resource privateKeyResource;

    @Value("classpath:keys/public.pem")
    private Resource publicKeyResource;

    @Bean
    public RSAPrivateKey privateKey() throws Exception {
        String key = readKey(privateKeyResource);

        byte[] keyBytes = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    @Bean 
    public RSAPublicKey publicKey() throws Exception {
        String key = readKey(publicKeyResource);

        byte[] keyBytes = Base64.getDecoder().decode(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean 
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        return NimbusJwtEncoder.withKeyPair(publicKey, privateKey).build();
        
    }

    @Bean 
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey, @Value("${orion.security.jwt.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();

        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));

        return decoder;
    }

    private String readKey(Resource resource) throws Exception {
        try(InputStream inputStream = resource.getInputStream()) {
            String pem = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            return pem  
                .replace(
                    "-----BEGIN PRIVATE KEY-----",
                    ""
                )
                .replace(
                    "-----END PRIVATE KEY-----", 
                    ""
                )
                .replace(
                    "-----BEGIN PUBLIC KEY-----",
                    ""
                )
                .replace(
                    "-----END PUBLIC KEY-----", 
                    ""
                )
                .replaceAll("\\s", "");
        }
    }
}