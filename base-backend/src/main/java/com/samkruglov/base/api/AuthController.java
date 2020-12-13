package com.samkruglov.base.api;

import com.samkruglov.base.api.config.Current;
import com.samkruglov.base.api.config.OpenApiConfig;
import com.samkruglov.base.api.view.request.ChangePasswordDto;
import com.samkruglov.base.api.view.request.CredentialsDto;
import com.samkruglov.base.api.view.response.JwtDto;
import com.samkruglov.base.domain.User;
import com.samkruglov.base.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.samkruglov.base.api.config.UserUrlPathId.SELF;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @Tag(name = OpenApiConfig.INSECURE_TAG)
    @SneakyThrows
    @PostMapping("/login")
    public JwtDto login(@Valid @RequestBody CredentialsDto credentials) {
        return new JwtDto(service.login(credentials).serialize());
    }

    @PutMapping("/users/" + SELF + "/change-password")
    public void changeMyPassword(@Current User user, @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        service.changePassword(user, changePasswordDto);
    }
}
