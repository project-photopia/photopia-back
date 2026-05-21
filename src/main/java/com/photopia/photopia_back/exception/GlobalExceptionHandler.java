package com.photopia.photopia_back.exception;

import com.photopia.photopia_back.model.ApiError;
import com.photopia.photopia_back.model.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), "ACCESS_DENIED");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), "NOT_FOUND");
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleSpringAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access denied", "ACCESS_DENIED");
    }

    @ExceptionHandler({ AuthenticationException.class, AuthenticationCredentialsNotFoundException.class })
    public ResponseEntity<ApiErrorResponse> handleAuthentication(Exception ex) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication required", "UNAUTHORIZED");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        String lower = message.toLowerCase();

        if (lower.contains("not a member")
                || lower.contains("forbidden")
                || lower.contains("only admins")
                || lower.contains("owner cannot")
                || lower.contains("cannot remove")
                || lower.contains("not authorized")) {
            return build(HttpStatus.FORBIDDEN, message, "ACCESS_DENIED");
        }

        if (lower.contains("not found")) {
            return build(HttpStatus.NOT_FOUND, message, "NOT_FOUND");
        }

        if (lower.contains("invalid")
                || lower.contains("already")
                || lower.contains("must")
                || lower.contains("cannot ")) {
            return build(HttpStatus.BAD_REQUEST, message, "BAD_REQUEST");
        }

        log.error("Unhandled RuntimeException", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, "INTERNAL_ERROR");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "INTERNAL_ERROR");
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String code) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .message(message)
                .code(code)
                .build();
        return ResponseEntity.status(status).body(ApiErrorResponse.of(error));
    }
}
