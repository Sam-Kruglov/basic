package com.samkruglov.base.api.view;

import lombok.Value;

@Value
public class CreateUserDto {
    String email;
    String password;
}
