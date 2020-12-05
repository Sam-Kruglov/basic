package com.samkruglov.base.api.view.request;

import lombok.Value;

@Value
public class CreateUserDto {
    String firstName;
    String lastName;
    String email;
    String password;
}