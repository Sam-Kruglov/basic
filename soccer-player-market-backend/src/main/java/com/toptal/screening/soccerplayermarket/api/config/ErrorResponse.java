package com.toptal.screening.soccerplayermarket.api.config;

import com.toptal.screening.soccerplayermarket.service.error.SoccerMarketErrorType;
import lombok.Value;

@Value
public class ErrorResponse {

    /**
     * Refers to {@link SoccerMarketErrorType#errorCode}
     */
    Integer code;
    String message;
}
