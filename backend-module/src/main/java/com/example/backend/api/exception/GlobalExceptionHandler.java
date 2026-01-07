package com.example.backend.api.exception;

import com.example.backend.api.dto.response.ApiErrorResponseDto;
import com.example.backend.api.dto.response.ApiFieldError;
import com.example.ejb.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(EntityNotFoundException ignored) {
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponseDto handleBusinessException(BusinessException ex) {
        return ApiErrorResponseDto.of("BUSINESS_EXCEPTION", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponseDto handleBodyValidation(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiFieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return ApiErrorResponseDto.of("VALIDATION_ERROR", "Invalid request body", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponseDto handleConstraintViolations(ConstraintViolationException ex) {
        var violations = ex.getConstraintViolations().stream()
                .map(v -> new ApiFieldError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        return ApiErrorResponseDto.of("VALIDATION_ERROR", "Constraint violation", violations);
    }

    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponseDto handleTransactionSystem(TransactionSystemException ex) {
        var root = rootCause(ex);

        if (root instanceof ConstraintViolationException cve) {
            var violations = cve.getConstraintViolations().stream()
                    .map(v -> new ApiFieldError(v.getPropertyPath().toString(), v.getMessage()))
                    .toList();
            return ApiErrorResponseDto.of("VALIDATION_ERROR", "Constraint violation", violations);
        }

        return ApiErrorResponseDto.of("TRANSACTION_ERROR", "Transaction failed");
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}
