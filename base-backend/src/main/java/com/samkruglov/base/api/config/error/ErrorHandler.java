package com.samkruglov.base.api.config.error;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.samkruglov.base.api.config.ReferredUserConfig;
import com.samkruglov.base.service.error.BaseErrorType;
import com.samkruglov.base.service.error.BaseException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> expected(BaseException e) {
        val errorType = e.getErrorType();
        val message = createMessage(e, errorType);
        log.debug(message);
        return ResponseEntity.status(errorType.httpStatusCode)
                             .body(new ErrorResponse(errorType.errorCode, message));
    }

    /**
     * delegating to {@link ExceptionTranslationFilter}
     */
    @ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
    public Object auth(RuntimeException e) {
        throw e;
    }

    /**
     * This comes from failed method parameter validation via {@link Validated}.
     * This annotation will add an AOP proxy on the class and look for JSR 303 annotations.
     */
    //todo check up on https://github.com/spring-projects/spring-framework/issues/26219
    @ExceptionHandler
    ResponseEntity<Object> validation(ConstraintViolationException e) {
        if (CollectionUtils.isEmpty(e.getConstraintViolations())) {
            log.error("no constraint violations found");
            return unexpected(e);
        }
        val methodOrFieldOwnerClass = e.getConstraintViolations().iterator().next().getRootBeanClass();
        //if this doesn't comes from a controller parameter, then it's not related to API,
        // so it would be a server error instead of a client error.
        if (!methodOrFieldOwnerClass.isNestmateOf(ReferredUserConfig.class)
                && !AnnotatedElementUtils.hasAnnotation(methodOrFieldOwnerClass, Controller.class)) {
            return unexpected(e);
        }
        return buildValidationResponse(
                Optional.ofNullable(e.getMessage()),
                e.getConstraintViolations().stream()
                 .map(v -> new InvalidRequestParameter(getLast(v.getPropertyPath()).getName(), v.getMessage()))
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request
    ) {
        if (ex.getCause() != null && MismatchedInputException.class.isAssignableFrom(ex.getCause().getClass())) {
            val mismatchedInputException = ((MismatchedInputException) ex.getCause());
            val propertyPath = mismatchedInputException.getPath()
                                                       .stream()
                                                       .map(JsonMappingException.Reference::getFieldName)
                                                       .collect(joining("."));
            return wrongType(propertyPath);
        }
        return unexpected(ex);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request
    ) {
        val rootCause = ExceptionUtils.getRootCause(ex);
        // there may be exceptions thrown in our own custom type converters
        if (rootCause instanceof ConstraintViolationException) {
            return validation(((ConstraintViolationException) rootCause));
        }
        if (rootCause instanceof BaseException) {
            return expected(((BaseException) rootCause));
        }
        String propertyName;
        if (ex.getPropertyName() != null) {
            //some subtypes of this exception don't populate this field
            propertyName = ex.getPropertyName();
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            propertyName = ((MethodArgumentTypeMismatchException) ex).getName();
        } else {
            return unexpected(ex);
        }
        return wrongType(propertyName);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request
    ) {
        return buildValidationResponse(ex.getVariableName(), "missing path parameter");
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request
    ) {
        return buildValidationResponse(ex.getParameterName(), "missing request parameter");
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
    public ResponseEntity<Object> unexpected(Exception e) {
        log.error(e.getMessage(), e);
        val errorType = BaseErrorType.INTERNAL_ERROR;
        return ResponseEntity.status(errorType.httpStatusCode)
                             .body(new ErrorResponse(errorType.errorCode, errorType.description));
    }

    private ResponseEntity<Object> wrongType(String propertyPath) {
        return buildValidationResponse(propertyPath, "value of this type is not allowed");
    }

    private ResponseEntity<Object> buildValidationResponse(String parameterName, String message) {
        return buildValidationResponse(
                Optional.empty(),
                Stream.of(new InvalidRequestParameter(parameterName, message))
        );
    }

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
