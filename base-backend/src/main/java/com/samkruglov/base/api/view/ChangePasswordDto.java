package com.samkruglov.base.api.view;

import lombok.Value;

@Value
public class ChangePasswordDto {
    String oldPassword;
    String newPassword;
}
