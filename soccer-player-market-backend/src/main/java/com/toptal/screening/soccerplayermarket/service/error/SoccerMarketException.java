package com.toptal.screening.soccerplayermarket.service.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SoccerMarketException extends RuntimeException {

    private final SoccerMarketErrorType errorType;

    public SoccerMarketException(SoccerMarketErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public SoccerMarketException(SoccerMarketErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
}
