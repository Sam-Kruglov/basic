package com.samkruglov.base.api.config.error;

import com.samkruglov.base.service.error.BaseErrorType;
import lombok.Value;

@Value
public class ErrorResponse {

    /**
     * Refers to {@link BaseErrorType#errorCode}
     */
    Integer code;
    String message;
}
