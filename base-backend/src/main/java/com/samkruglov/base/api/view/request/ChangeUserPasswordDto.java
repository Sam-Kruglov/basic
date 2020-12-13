package com.samkruglov.base.api.view.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
@AllArgsConstructor(onConstructor_ = @JsonCreator)
//todo check up on https://github.com/FasterXML/jackson-databind/issues/2984
public class ChangeUserPasswordDto {
    @NotBlank String newPassword;
}
