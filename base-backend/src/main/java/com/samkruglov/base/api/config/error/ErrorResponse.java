package com.samkruglov.base.api.config.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.samkruglov.base.service.error.BaseErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

import static com.samkruglov.base.service.error.BaseErrorType.Constants.INVALID_REQUEST_CODE;

@Value
@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
public class ErrorResponse {

    /**
     * Refers to {@link BaseErrorType#errorCode}
     */
    @Schema(required = true)
    @NonNull Integer code;

    @Schema(required = true)
    @NonNull String message;

    @Schema(description = "May be present when the code is " + INVALID_REQUEST_CODE)
    List<InvalidRequestParameter> invalidRequestParameters;

    public ErrorResponse(@NonNull Integer code, @NonNull String message) {
        this(code, message, null);
    }
}
