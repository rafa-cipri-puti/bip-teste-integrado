package com.example.backend.api.mapper.impl;

import com.example.backend.api.dto.request.BeneficioRequestDto;
import com.example.backend.api.mapper.Mapper;
import com.example.domain.Beneficio;

import java.util.Objects;

public class BeneficioRequestDtoMapper implements Mapper<BeneficioRequestDto, Beneficio> {
    @Override
    public Beneficio map(BeneficioRequestDto input) {
        final Beneficio beneficio = new Beneficio();
        beneficio.setNome(input.getNome());
        beneficio.setDescricao(input.getDescricao());
        beneficio.setValor(input.getValor());
        beneficio.setAtivo(Objects.nonNull(input.getAtivo()) ? input.getAtivo() : true);
        return beneficio;
    }
}
