package com.example.backend.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponseDto(String code, String message, List<ApiFieldError> errors) {
    public static ApiErrorResponseDto of(String code, String message) {
        return new ApiErrorResponseDto(code, message, List.of());
    }

    public static ApiErrorResponseDto of(String code, String message, List<ApiFieldError> errors) {
        return new ApiErrorResponseDto(code, message, errors);
    }
}

