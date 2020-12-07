package com.samkruglov.base.service.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BaseErrorType {
    INTERNAL_ERROR(0, 500, "Internal error"),
    USER_NOT_FOUND(1, 404, "User not found"),
    EMAIL_ALREADY_EXISTS(2, 400, "Email already exists"),
    OLD_PASSWORD_DOES_NOT_MATCH(3, 400, "Old password does not match"),
    INVALID_REQUEST(Constants.INVALID_REQUEST_CODE, 400, "Invalid request format");

    public final int    errorCode;
    public final int    httpStatusCode;
    public final String description;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Constants {
        public static final int INVALID_REQUEST_CODE = 4;
    }
}
