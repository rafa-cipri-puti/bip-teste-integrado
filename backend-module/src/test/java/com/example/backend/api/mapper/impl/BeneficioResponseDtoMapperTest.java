package com.example.backend.api.mapper.impl;

import com.example.backend.api.dto.response.BeneficioResponseDto;
import com.example.domain.Beneficio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BeneficioResponseDtoMapperTest {

    private final BeneficioResponseDtoMapper mapper = new BeneficioResponseDtoMapper();

    @Test
    void map__shouldMapAllFieldsCorrectly() {
        Beneficio beneficio = new Beneficio();
        beneficio.setNome("Vale Refeição");
        beneficio.setDescricao("Benefício mensal");
        beneficio.setValor(new BigDecimal("500.00"));
        beneficio.setAtivo(true);

        BeneficioResponseDto dto = mapper.map(beneficio);

        assertNotNull(dto);
        assertEquals("Vale Refeição", dto.getNome());
        assertEquals("Benefício mensal", dto.getDescricao());
        assertEquals(new BigDecimal("500.00"), dto.getValor());
        assertTrue(dto.isAtivo());
    }

    @Test
    void map__whenNullFields__shouldPropagateNull() {
        Beneficio beneficio = new Beneficio();

        BeneficioResponseDto dto = mapper.map(beneficio);

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getNome());
        assertNull(dto.getDescricao());
        assertNull(dto.getValor());
    }
}
