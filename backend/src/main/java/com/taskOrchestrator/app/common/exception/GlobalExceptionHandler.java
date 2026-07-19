package com.taskOrchestrator.app.common.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> baseResponse(String message, String code) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("message", message);
        response.put("code", code);
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, Object> response = baseResponse("Validation failed", "VALIDATION_ERROR");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(JobNotFoundException ex) {
        Map<String, Object> response = baseResponse(ex.getMessage(), "JOB_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> response = baseResponse(ex.getMessage(), "INVALID_JOB_STATE");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Expired token reaching controller layer (e.g. /auth/refresh) → 401.
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpiredException(ExpiredJwtException ex) {
        Map<String, Object> response = baseResponse("Token has expired", "TOKEN_EXPIRED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Other JWT errors (malformed, bad signature, unsupported) → 401, not 500.
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        Map<String, Object> response = baseResponse("Invalid token", "INVALID_TOKEN");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Honor the embedded status (e.g. 401 from AuthService.refresh) instead of
    // falling through to the catch-all Exception handler.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        String code = ex.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()
                ? "UNAUTHORIZED"
                : "ERROR";
        Map<String, Object> response = baseResponse(reason, code);
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    // @PreAuthorize denials surface as AccessDeniedException; map to 403 rather than 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> response = baseResponse("Access denied", "FORBIDDEN");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralExceptions(Exception ex) {
        Map<String, Object> response = baseResponse(
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR"
        );

        //log the real exception
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(
            DuplicateEmailException exception) {
        Map<String, Object> response = baseResponse("Email already exists", "CONFLICT_EMAIL");
        //log the real exception
        //exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateUsername(
            DuplicateUsernameException exception
    ) {
        Map<String, Object> response = baseResponse("Username already exists", "CONFLICT_USERNAME");
        //log the real exception
        //exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
