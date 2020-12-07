package com.samkruglov.base.api.view.request;

import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
public class CredentialsDto {
    @NotBlank @Email String email;
    @NotBlank        String password;
}
