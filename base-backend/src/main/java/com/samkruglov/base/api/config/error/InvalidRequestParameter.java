package com.samkruglov.base.api.config.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;
import lombok.Value;

@Value
public class InvalidRequestParameter {
    @Schema(required = true)
    @NonNull String name;

    @Schema(required = true)
    @NonNull String message;
}
