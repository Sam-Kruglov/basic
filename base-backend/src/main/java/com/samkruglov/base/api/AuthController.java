package com.samkruglov.base.api;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.samkruglov.base.config.SecurityConfig;
import com.samkruglov.base.domain.Role;
import com.samkruglov.base.api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWSSigner jwsSigner;
    private final JWSAlgorithm jwsAlgorithm;

    @Value("${jwt.valid-for}")
    @Setter
    private Duration validFor;

    public AuthController(
            AuthenticationManager authenticationManager,
            JWSSigner jwsSigner,
            OAuth2ResourceServerProperties properties
    ) {
        this.authenticationManager = authenticationManager;
        this.jwsSigner = jwsSigner;
        this.jwsAlgorithm = JWSAlgorithm.parse(properties.getJwt().getJwsAlgorithm());
    }

    @Tag(name = OpenApiConfig.INSECURE_TAG)
    @SneakyThrows
    @PostMapping("/login")
    public JwtDto login(@RequestBody CredentialsDto credentials) {
        val user = ((SecurityConfig.CustomUser) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.getEmail(), credentials.getPassword())
        ).getPrincipal()).getDelegate();
        val jwt = new SignedJWT(
                new JWSHeader.Builder(jwsAlgorithm).build(),
                new JWTClaimsSet.Builder()
                        .subject(user.getEmail())
                        .issueTime(Date.from(Instant.now()))
                        .expirationTime(Date.from(Instant.now().plus(validFor)))
                        .claim(
                                OAuth2IntrospectionClaimNames.SCOPE,
                                user.getRoles()
                                    .stream()
                                    .map(Role::getName)
                                    .collect(Collectors.toUnmodifiableList())
                        )
                        .build());
        jwt.sign(jwsSigner);
        return new JwtDto(jwt.serialize());
    }

    @lombok.Value
    public static class JwtDto {
        String jwt;
    }

    @lombok.Value
    public static class CredentialsDto {
        String email;
        String password;
    }
}