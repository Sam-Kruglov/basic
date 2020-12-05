package com.samkruglov.base.api.view.request;

import lombok.Value;

@Value
public class ChangePasswordDto {
    String oldPassword;
    String newPassword;
}
