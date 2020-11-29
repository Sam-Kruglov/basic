package com.samkruglov.base.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.samkruglov.base.api.view.ChangePasswordDto;
import com.samkruglov.base.api.view.CredentialsDto;
import com.samkruglov.base.config.SecurityConfig;
import com.samkruglov.base.domain.Role;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.repo.UserRepo;
import com.samkruglov.base.service.error.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

import static com.samkruglov.base.service.error.BaseErrorType.OLD_PASSWORD_DOES_NOT_MATCH;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWSSigner jwsSigner;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.valid-for}")
    private final Duration validFor;
    private JWSAlgorithm jwsAlgorithm;

    @Autowired
    public void setJwsAlgorithm(OAuth2ResourceServerProperties properties) {
        this.jwsAlgorithm = JWSAlgorithm.parse(properties.getJwt().getJwsAlgorithm());
    }

    @SneakyThrows
    public SignedJWT login(CredentialsDto credentials) {
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
        return jwt;
    }

    public void changePassword(User user, ChangePasswordDto changePasswordDto) {
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getEncodedPassword())) {
            throw new BaseException(OLD_PASSWORD_DOES_NOT_MATCH);
        }
        user.setEncodedPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepo.save(user);
    }
}
