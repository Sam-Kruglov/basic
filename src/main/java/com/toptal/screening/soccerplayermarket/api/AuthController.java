package com.toptal.screening.soccerplayermarket.api;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.toptal.screening.soccerplayermarket.config.SecurityConfig;
import com.toptal.screening.soccerplayermarket.domain.Role;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWSSigner jwsSigner;
    private final OAuth2ResourceServerProperties properties;

    @Value("${jwt.valid-for}")
    @Setter
    private Duration validFor;

    @SneakyThrows
    @PostMapping("/login")
    public JwtDto login(@RequestBody CredentialsDto credentials) {
        val user = ((SecurityConfig.CustomUser) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.getEmail(), credentials.getPassword())
        ).getPrincipal()).getDelegate();
        val jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.parse(properties.getJwt().getJwsAlgorithm())).build(),
                new JWTClaimsSet.Builder()
                        .subject(user.getEmail())
                        .issueTime(Date.from(Instant.now()))
                        .expirationTime(Date.from(Instant.now().plusMillis(validFor.toMillis())))
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
