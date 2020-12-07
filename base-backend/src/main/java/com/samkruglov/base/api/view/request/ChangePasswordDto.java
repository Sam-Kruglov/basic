package com.samkruglov.base.api.view.request;

import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class ChangePasswordDto {
    @NotBlank String oldPassword;
    @NotBlank String newPassword;
}
