package com.example.backend.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferResponseDto {
    private BeneficioResponseDto fromBeneficio;
    private BeneficioResponseDto toBeneficio;
}
