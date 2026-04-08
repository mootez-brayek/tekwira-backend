package com.project.tekwira.Exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("data", data);
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }

    // 🔹 Validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "Validation error";
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage, null);
    }

    // 🔹 JSON parse errors
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableJson(HttpMessageNotReadableException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "Malformed JSON request";
        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    // 🔹 DB constraint violations (duplicate email / phone)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Duplicate entry detected";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("phone_number")) {
                message = "Phone number already used";
            } else if (ex.getMessage().contains("email")) {
                message = "Email already used";
            }
        }
        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String message;

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String allowedValues = String.join(", ",
                    Arrays.stream(ex.getRequiredType().getEnumConstants())
                            .map(Object::toString)
                            .toList());

            message = messageSource.getMessage(
                    "invalid.enum.value",
                    new Object[]{ex.getValue(), ex.getName(), allowedValues},
                    locale
            );
        } else {
            message = messageSource.getMessage(
                    "invalid.param.type",
                    new Object[]{ex.getName()},
                    locale
            );
        }

        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }


    // 🔹 JWT token errors (invalid, expired, mismatched, etc.)
    @ExceptionHandler({JwtException.class, ExpiredJwtException.class})
    public ResponseEntity<Map<String, Object>> handleJwtException(Exception ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid or expired token. Please log in again.", null);
    }

    // 🔹 Invalid credentials / user not found
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthErrors(Exception ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid authentication credentials. Please log in again.", null);
    }

    // 🔹 Access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.", null);
    }

    // 🔹 Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();

        String message = messageSource.getMessage(
                "missingRequestParameter",
                new Object[]{paramName},
                LocaleContextHolder.getLocale()
        );

        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

}
