package com.samkruglov.base.api.view.request;

import lombok.Value;

@Value
public class ChangeUserDto {
    String firstName;
    String lastName;
}