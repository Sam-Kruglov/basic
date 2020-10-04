package com.toptal.screening.soccerplayermarket.api.config;

import lombok.Value;

@Value
public class ErrorResponse {
    Integer code;
    String message;
}
