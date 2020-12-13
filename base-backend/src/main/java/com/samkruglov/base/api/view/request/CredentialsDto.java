package com.samkruglov.base.api.view.request;

import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class CredentialsDto {
    @NotBlank @Email @Size(min = 2, max = 70) String email;
    @NotBlank                                 String password;
}
