package com.example.backend.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDto {
    @NotNull
    @Positive
    private Long fromId;
    @NotNull
    @Positive
    private Long toId;
    @Positive
    @NotNull
    private BigDecimal amount;
}
