package com.example.backend.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BeneficioResponseDto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private boolean ativo;
}
