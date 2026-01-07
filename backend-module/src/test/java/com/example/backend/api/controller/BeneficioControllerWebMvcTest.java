package com.example.backend.api.controller;

import com.example.backend.service.BeneficioService;
import com.example.domain.Beneficio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeneficioController.class)
class BeneficioControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BeneficioService beneficioService;

    @Test
    void getAll__shouldReturn200__andDtoPages() throws Exception {
        Beneficio b1 = beneficio(1L, "A", "D1", "10.00", true);
        Beneficio b2 = beneficio(2L, "B", "D2", "20.00", false);

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<Beneficio> page = new PageImpl<>(List.of(b1, b2), pageable, 2);

        when(beneficioService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/beneficios")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("A"))
                .andExpect(jsonPath("$.content[0].descricao").value("D1"))
                .andExpect(jsonPath("$.content[0].valor").value(10.00))
                .andExpect(jsonPath("$.content[0].ativo").value(true));

        verify(beneficioService).getAll(any(Pageable.class));
        verifyNoMoreInteractions(beneficioService);
    }

    @Test
    void create__shouldReturn201__andDtoCreated() throws Exception {
        Beneficio saved = beneficio(10L, "VR", "Mensal", "500.00", true);

        when(beneficioService.create(any(Beneficio.class))).thenReturn(saved);

        String body = """
                {
                  "nome": "VR",
                  "descricao": "Mensal",
                  "valor": 500.00,
                  "ativo": true
                }
                """;

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("VR"))
                .andExpect(jsonPath("$.descricao").value("Mensal"))
                .andExpect(jsonPath("$.valor").value(500.00))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(beneficioService).create(any(Beneficio.class));
        verifyNoMoreInteractions(beneficioService);
    }

    @Test
    void update__shouldReturn200__andDtoUpdated() throws Exception {
        Beneficio updated = beneficio(5L, "Novo", "Desc", "123.45", false);

        when(beneficioService.update(eq(5L), any(Beneficio.class))).thenReturn(updated);

        String body = """
                {
                  "nome": "Novo",
                  "descricao": "Desc",
                  "valor": 123.45,
                  "ativo": false
                }
                """;

        mockMvc.perform(put("/api/v1/beneficios/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nome").value("Novo"))
                .andExpect(jsonPath("$.descricao").value("Desc"))
                .andExpect(jsonPath("$.valor").value(123.45))
                .andExpect(jsonPath("$.ativo").value(false));

        verify(beneficioService).update(eq(5L), any(Beneficio.class));
        verifyNoMoreInteractions(beneficioService);
    }

    @Test
    void delete__shouldReturn204() throws Exception {
        doNothing().when(beneficioService).delete(7L);

        mockMvc.perform(delete("/api/v1/beneficios/{id}", 7))
                .andExpect(status().isNoContent());

        verify(beneficioService).delete(7L);
        verifyNoMoreInteractions(beneficioService);
    }

    @Test
    void transfer__shouldReturn200__andDtosFromTo__andCallService() throws Exception {
        Long fromId = 1L;
        Long toId = 2L;
        BigDecimal amount = new BigDecimal("50.00");

        doNothing().when(beneficioService).transfer(fromId, toId, amount);

        when(beneficioService.getById(fromId)).thenReturn(beneficio(fromId, "From", "A", "100.00", true));
        when(beneficioService.getById(toId)).thenReturn(beneficio(toId, "To", "B", "200.00", true));

        String body = """
                {
                  "fromId": 1,
                  "toId": 2,
                  "amount": 50.00
                }
                """;

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromBeneficio.id").value(1))
                .andExpect(jsonPath("$.fromBeneficio.nome").value("From"))
                .andExpect(jsonPath("$.toBeneficio.id").value(2))
                .andExpect(jsonPath("$.toBeneficio.nome").value("To"));

        verify(beneficioService).transfer(fromId, toId, amount);
        verify(beneficioService, times(1)).getById(fromId);
        verify(beneficioService, times(1)).getById(toId);
        verifyNoMoreInteractions(beneficioService);
    }

    private static Beneficio beneficio(Long id, String nome, String descricao, String valor, boolean ativo) {
        Beneficio b = new Beneficio();
        b.setNome(nome);
        b.setDescricao(descricao);
        b.setValor(new BigDecimal(valor));
        b.setAtivo(ativo);
        ReflectionTestUtils.setField(b, "id", id);
        return b;
    }
}
