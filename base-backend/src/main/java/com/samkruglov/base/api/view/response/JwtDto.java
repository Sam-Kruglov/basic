package com.samkruglov.base.api.view.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;
import lombok.Value;

@Value
public class JwtDto {
    @Schema(required = true)
    @NonNull String jwt;
}
