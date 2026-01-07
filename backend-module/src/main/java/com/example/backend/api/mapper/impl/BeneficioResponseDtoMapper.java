package com.example.backend.api.mapper.impl;

import com.example.backend.api.dto.response.BeneficioResponseDto;
import com.example.backend.api.mapper.Mapper;
import com.example.domain.Beneficio;

public class BeneficioResponseDtoMapper implements Mapper<Beneficio, BeneficioResponseDto> {
    @Override
    public BeneficioResponseDto map(Beneficio input) {
        return BeneficioResponseDto.builder()
                .id(input.getId())
                .nome(input.getNome())
                .valor(input.getValor())
                .descricao(input.getDescricao())
                .ativo(input.getAtivo())
                .build();
    }
}
