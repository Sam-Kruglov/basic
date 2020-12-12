package com.samkruglov.base.api.view.request;

import com.samkruglov.base.api.view.constraint.UserFirstOrSecondName;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class CreateUserDto {
    @NotNull @UserFirstOrSecondName String firstName;
    @NotNull @UserFirstOrSecondName String lastName;
    @NotBlank @Email String email;
    @NotBlank        String password;
}
