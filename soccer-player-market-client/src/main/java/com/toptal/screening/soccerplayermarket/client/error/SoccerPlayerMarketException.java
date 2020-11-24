package com.toptal.screening.soccerplayermarket.client.error;

import com.toptal.screening.soccerplayermarket.client.gen.view.ErrorResponse.CodeEnum;

public class SoccerPlayerMarketException extends RuntimeException {
    private final CodeEnum errorCode;

    public SoccerPlayerMarketException(CodeEnum errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public SoccerPlayerMarketException(CodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SoccerPlayerMarketException(CodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public CodeEnum getErrorCode() {
        return errorCode;
    }
}
