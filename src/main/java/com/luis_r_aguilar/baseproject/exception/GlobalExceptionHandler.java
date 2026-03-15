package com.luis_r_aguilar.baseproject.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadCredentialsException.class)
    public ErrorResponse handleNotFound(BadCredentialsException ex) {
        LOGGER.warn("Credentials error: {}", ex.getMessage());
        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ErrorResponse handleValidationException(BindException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        LOGGER.warn("Validation error: {}", errors);

        return new ErrorResponse(
                Instant.now(),
                "Validation incorrect",
                errors
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleBadRequest(IllegalArgumentException ex) {
        LOGGER.warn("Bad request: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ErrorResponse handleNotFound(RuntimeException ex) {
        LOGGER.warn("Resource not found: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        LOGGER.warn("Method not allowed: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ErrorResponse handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        LOGGER.warn("Unsupported media type: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ErrorResponse handleUnauthorized(UnauthorizedException ex) {
        LOGGER.warn("Unauthorized: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public ErrorResponse handleForbidden(ForbiddenException ex) {
        LOGGER.warn("Forbidden: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ErrorResponse handleConflict(ConflictException ex) {
        LOGGER.warn("Conflict: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ExceptionHandler(TooManyRequestsException.class)
    public ErrorResponse handleTooManyRequests(TooManyRequestsException ex) {
        LOGGER.warn("Too many requests: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public ErrorResponse handleServiceUnavailable(ServiceUnavailableException ex) {
        LOGGER.error("Service unavailable: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    @ExceptionHandler(GatewayTimeoutException.class)
    public ErrorResponse handleGatewayTimeout(GatewayTimeoutException ex) {
        LOGGER.error("Gateway timeout: {}", ex.getMessage());

        return new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                null
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleInternalServerError(Exception ex) {
        LOGGER.error("Unexpected error occurred", ex);

        return new ErrorResponse(
                Instant.now(),
                "An unexpected error occurred",
                null
        );
    }

}
