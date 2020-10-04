package com.toptal.screening.soccerplayermarket.service.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SoccerMarketErrorType {
    INTERNAL_ERROR(0, 500, "Internal error"),
    USER_NOT_FOUND(1, 404, "User not found"),
    EMAIL_ALREADY_EXISTS(2, 400, "Email already exists");

    public final int errorCode;
    public final int httpStatusCode;
    public final String description;

}
