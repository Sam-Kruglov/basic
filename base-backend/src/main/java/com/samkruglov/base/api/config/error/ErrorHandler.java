package com.samkruglov.base.api.config.error;

import com.samkruglov.base.service.error.BaseErrorType;
import com.samkruglov.base.service.error.BaseException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> expected(BaseException e) {
        val errorType = e.getErrorType();
        val message = createMessage(e, errorType);
        log.debug(message);
        return ResponseEntity.status(errorType.httpStatusCode)
                             .body(new ErrorResponse(errorType.errorCode, message));
    }

    /**
     * delegating to {@link ExceptionTranslationFilter}
     */
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public Object auth(RuntimeException e) {
        throw e;
    }

    /**
     * This comes from failed method parameter validation via {@link Validated}.
     * This annotation will add an AOP proxy on the class and look for JSR 303 annotations.
     */
    //todo check up on https://github.com/spring-projects/spring-framework/issues/26219
    @ExceptionHandler
    ResponseEntity<?> validation(ConstraintViolationException e) {
        if (CollectionUtils.isEmpty(e.getConstraintViolations())) {
            return unexpected(e);
        }
        val methodOwnerClass = e.getConstraintViolations().iterator().next().getRootBeanClass();
        //if this doesn't comes from a controller parameter, then it's not related to API,
        // so it would be a server error instead of a client error.
        if (!AnnotatedElementUtils.hasAnnotation(methodOwnerClass, Controller.class)) {
            return unexpected(e);
        }
        return buildValidationResponse(
                Optional.ofNullable(e.getMessage()),
                e.getConstraintViolations().stream()
                 .map(v -> new InvalidRequestParameter(getLast(v.getPropertyPath()).getName(), v.getMessage()))
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        return handleBindException(ex, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(
            BindException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        return buildValidationResponse(
                Optional.ofNullable(ex.getBindingResult().getGlobalError()).map(DefaultMessageSourceResolvable::getDefaultMessage),
                ex.getBindingResult().getFieldErrors().stream()
                  .map(f -> new InvalidRequestParameter(f.getField(), f.getDefaultMessage()))
        );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> unexpected(Exception e) {
        log.error(e.getMessage(), e);
        val errorType = BaseErrorType.INTERNAL_ERROR;
        return ResponseEntity.status(errorType.httpStatusCode)
                             .body(new ErrorResponse(errorType.errorCode, errorType.description));
    }

    //there are other potential client exceptions that are handled in parent class but aren't relevant with the current API,
    // like missing path/request parameter or wrong type.
    private ResponseEntity<Object> buildValidationResponse(
            Optional<String> messageOpt,
            Stream<InvalidRequestParameter> parameterStream
    ) {
        val errorType = BaseErrorType.INVALID_REQUEST;
        val invalidParams = parameterStream.collect(toList());
        return ResponseEntity.status(errorType.httpStatusCode)
                             .body(new ErrorResponse(
                                     errorType.errorCode,
                                     messageOpt.orElse(errorType.description),
                                     invalidParams
                             ));
    }

    private <T> T getLast(Iterable<T> iterable) {
        val iterator = iterable.iterator();
        T element = null;
        while (iterator.hasNext()) {
            element = iterator.next();
        }
        return element;
    }

    private String createMessage(Exception e, BaseErrorType errorType) {
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
