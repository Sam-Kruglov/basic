package com.samkruglov.base.client.error;

import com.samkruglov.base.client.gen.view.ErrorResponse.CodeEnum;

public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final CodeEnum errorCode;

    public BaseException(CodeEnum errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public BaseException(CodeEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(CodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public CodeEnum getErrorCode() {
        return errorCode;
    }
}
