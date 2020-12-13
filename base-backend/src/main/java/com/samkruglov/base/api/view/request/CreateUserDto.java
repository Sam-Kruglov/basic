package com.samkruglov.base.api.view.request;

import com.samkruglov.base.api.view.constraint.UserFirstOrSecondName;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Value
public class CreateUserDto {
    @NotNull @UserFirstOrSecondName           String firstName;
    @NotNull @UserFirstOrSecondName           String lastName;
    @NotBlank @Email @Size(min = 2, max = 70) String email;
    @NotBlank                                 String password;
}
