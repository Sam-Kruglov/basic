package com.samkruglov.base.service.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BaseException extends RuntimeException {

    private final BaseErrorType errorType;

    public BaseException(BaseErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public BaseException(BaseErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
}
