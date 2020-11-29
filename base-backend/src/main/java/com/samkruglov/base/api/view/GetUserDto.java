package com.samkruglov.base.api.view;

import lombok.Value;

@Value
public class GetUserDto {
    String email;
    String firstName;
    String lastName;
}
