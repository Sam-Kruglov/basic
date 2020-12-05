package com.samkruglov.base.api.view.request;

import lombok.Value;

@Value
public class CredentialsDto {
    String email;
    String password;
}
