package com.samkruglov.base.api;

import com.samkruglov.base.api.config.OpenApiConfig;
import com.samkruglov.base.api.view.CredentialsDto;
import com.samkruglov.base.api.view.JwtDto;
import com.samkruglov.base.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @Tag(name = OpenApiConfig.INSECURE_TAG)
    @SneakyThrows
    @PostMapping("/login")
    public JwtDto login(@RequestBody CredentialsDto credentials) {
        return new JwtDto(service.login(credentials).serialize());
    }
}
