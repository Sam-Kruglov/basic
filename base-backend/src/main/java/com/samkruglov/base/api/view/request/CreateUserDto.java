package com.samkruglov.base.api.view.request;

import com.samkruglov.base.api.view.constraint.UserFirstOrSecondName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class CreateUserDto {
    @Schema(required = true)
    @NotNull @UserFirstOrSecondName String firstName;

    @Schema(required = true)
    @NotNull @UserFirstOrSecondName String lastName;

    @NotBlank @Email String email;
    @NotBlank        String password;
}
