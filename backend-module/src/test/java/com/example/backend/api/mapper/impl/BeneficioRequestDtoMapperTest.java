package com.example.backend.api.mapper.impl;

import com.example.backend.api.dto.request.BeneficioRequestDto;
import com.example.domain.Beneficio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BeneficioRequestDtoMapperTest {

    private final BeneficioRequestDtoMapper mapper = new BeneficioRequestDtoMapper();

    @Test
    void map__shouldMapAllFieldsWhenAtivoIsNonNull() {
        BeneficioRequestDto dto = BeneficioRequestDto.builder()
                .nome("Plano Saúde")
                .descricao("Cobertura nacional")
                .valor(new BigDecimal("1200.00"))
                .ativo(false)
                .build();

        Beneficio beneficio = mapper.map(dto);

        assertNotNull(beneficio);
        assertEquals("Plano Saúde", beneficio.getNome());
        assertEquals("Cobertura nacional", beneficio.getDescricao());
        assertEquals(new BigDecimal("1200.00"), beneficio.getValor());
        assertFalse(beneficio.getAtivo());
    }

    @Test
    void map__whenAtivoIsNull__shouldSetAtivoAsTrue() {
        BeneficioRequestDto dto = BeneficioRequestDto.builder()
                .nome("Vale Transporte")
                .descricao("Mensal")
                .valor(new BigDecimal("300.00"))
                .ativo(null)
                .build();

        Beneficio beneficio = mapper.map(dto);

        assertNotNull(beneficio);
        assertEquals("Vale Transporte", beneficio.getNome());
        assertEquals("Mensal", beneficio.getDescricao());
        assertEquals(new BigDecimal("300.00"), beneficio.getValor());
        assertTrue(beneficio.getAtivo());
    }

    @Test
    void map__whenNullFields__shouldPropagateNull__andAtivoTrue() {
        BeneficioRequestDto dto = new BeneficioRequestDto();

        Beneficio beneficio = mapper.map(dto);

        assertNotNull(beneficio);
        assertNull(beneficio.getNome());
        assertNull(beneficio.getDescricao());
        assertNull(beneficio.getValor());
        assertTrue(beneficio.getAtivo());
    }

    @Test
    void map__whenInputNull__shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> mapper.map(null));
    }
}
