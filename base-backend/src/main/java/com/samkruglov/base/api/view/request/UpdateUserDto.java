package com.samkruglov.base.api.view.request;

import com.samkruglov.base.api.view.constraint.AnyNotNull;
import com.samkruglov.base.api.view.constraint.UserFirstOrSecondName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
@AnyNotNull
@Schema(description = "at least one field must be present")
public class UpdateUserDto {
    @UserFirstOrSecondName String firstName;
    @UserFirstOrSecondName String lastName;
}
