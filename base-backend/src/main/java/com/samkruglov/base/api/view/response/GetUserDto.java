package com.samkruglov.base.api.view.response;

import lombok.Value;

@Value
public class GetUserDto {
    String email;
    String firstName;
    String lastName;
}
