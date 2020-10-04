package com.toptal.screening.soccerplayermarket.api.config;

import com.toptal.screening.soccerplayermarket.service.error.SoccerMarketErrorType;
import com.toptal.screening.soccerplayermarket.service.error.SoccerMarketException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;

@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> expected(SoccerMarketException e) {
        val errorType = e.getErrorType();
        val message = createMessage(e, errorType);
        log.debug(message);
        return ResponseEntity.status(errorType.httpStatusCode)
                             .body(new ErrorResponse(errorType.errorCode, message));
    }

    /**
     * delegating to {@link ExceptionTranslationFilter}
     */
    @ExceptionHandler
    public Object auth(AuthenticationException e) {
        throw e;
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> unexpected(Exception e) {
        log.error(e.getMessage(), e);
        val errorType = SoccerMarketErrorType.INTERNAL_ERROR;
        return ResponseEntity.status(errorType.httpStatusCode)
                             .body(new ErrorResponse(errorType.errorCode, errorType.description));
    }

    private String createMessage(Exception e, SoccerMarketErrorType errorType) {
        if (e.getMessage() != null) {
            return e.getMessage();
        }
        if (e.getCause() != null) {
            return errorType.description + ": " + Optional.ofNullable(e.getCause().getMessage())
                                                          .orElse(e.getCause().getClass().getName());
        }
        return errorType.description;
    }
}
