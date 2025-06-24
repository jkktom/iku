package org.mtvs.backend.auth.util;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class ClerkJwtVerifier {

    private final String clerkJwksUrl;

    public ClerkJwtVerifier(@Value("${clerk.domain}") String clerkDomain) {
        this.clerkJwksUrl = "https://" + clerkDomain + "/.well-known/jwks.json";
    }

    public JWTClaimsSet verifyAndGetClaims(String token) throws Exception {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(new URL(clerkJwksUrl)).build();
        JWSKeySelector<SecurityContext> keySelector =
            new com.nimbusds.jose.proc.JWSVerificationKeySelector<>(
                com.nimbusds.jose.JWSAlgorithm.RS256,
                jwkSource
            );
        jwtProcessor.setJWSKeySelector(keySelector);

        // This will throw if the token is invalid
        return jwtProcessor.process(token, null);
    }
}
