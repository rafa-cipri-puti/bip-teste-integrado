package com.example.backend.service;

import com.example.backend.infrastructure.repository.BeneficioRepository;
import com.example.domain.Beneficio;
import com.example.ejb.service.BeneficioEjbService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock
    private BeneficioEjbService beneficioEjbService;

    @Mock
    private BeneficioRepository beneficioRepository;

    @InjectMocks
    private BeneficioService service;

    private Beneficio actual;

    @BeforeEach
    void setup() {
        actual = new Beneficio();
        actual.setNome("Plano A");
        actual.setDescricao("Desc A");
        actual.setValor(new BigDecimal("10.00"));
        actual.setAtivo(true);
    }

    @Test
    void getAll__shouldReturnBeneficiosPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<Beneficio> page = new PageImpl<>(java.util.List.of(actual), pageable, 1);

        when(beneficioRepository.findAll(pageable)).thenReturn(page);

        Page<Beneficio> result = service.getAll(pageable);

        assertSame(page, result);
        verify(beneficioRepository).findAll(pageable);
        verifyNoMoreInteractions(beneficioRepository);
        verifyNoInteractions(beneficioEjbService);
    }

    @Test
    void getById__whenExists__shouldReturnBeneficio() {
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(actual));

        Beneficio result = service.getById(1L);

        assertSame(actual, result);
        verify(beneficioRepository).findById(1L);
        verifyNoMoreInteractions(beneficioRepository);
        verifyNoInteractions(beneficioEjbService);
    }

    @Test
    void getById__whenNotExists____shouldThrowEntityNotFoundException() {
        when(beneficioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getById(404L));

        verify(beneficioRepository).findById(404L);
        verifyNoMoreInteractions(beneficioRepository);
        verifyNoInteractions(beneficioEjbService);
    }

    @Test
    void create__shouldSaveAndReturn() {
        Beneficio toCreate = new Beneficio();
        toCreate.setNome("Novo");

        Beneficio saved = new Beneficio();
        saved.setNome("Novo");

        when(beneficioRepository.save(toCreate)).thenReturn(saved);

        Beneficio result = service.create(toCreate);

        assertSame(saved, result);
        verify(beneficioRepository).save(toCreate);
        verifyNoMoreInteractions(beneficioRepository);
        verifyNoInteractions(beneficioEjbService);
    }

    @Test
    void update__whenNotExists__shouldThrowEntityNotFoundException() {
        when(beneficioRepository.findById(99L)).thenReturn(Optional.empty());

        Beneficio updated = new Beneficio();
        updated.setNome("Qualquer");

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, updated));

        verify(beneficioRepository).findById(99L);
        verify(beneficioRepository, never()).save(any());
        verifyNoMoreInteractions(beneficioRepository);
        verifyNoInteractions(beneficioEjbService);
    }

    @Test
    void update__shouldReplaceOnlyNonNullFields__andSave() {
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(beneficioRepository.save(any(Beneficio.class))).thenAnswer(inv -> inv.getArgument(0));

        Beneficio patch = new Beneficio();
        patch.setNome("Plano B");
        patch.setDescricao(null); // deve manter "Desc A"
        patch.setValor(new BigDecimal("25.50"));
        patch.setAtivo(null); // deve manter true

        Beneficio result = service.update(1L, patch);

        assertEquals("Plano B", result.getNome());
        assertEquals("Desc A", result.getDescricao());
        assertEquals(new BigDecimal("25.50"), result.getValor());
        assertTrue(result.getAtivo());

        ArgumentCaptor<Beneficio> captor = ArgumentCaptor.forClass(Beneficio.class);
        verify(beneficioRepository).findById(1L);
        verify(beneficioRepository).save(captor.capture());

        Beneficio savedArg = captor.getValue();
        assertSame(actual, savedArg); // garante que salvou o "actual" modificado
        assertEquals("Plano B", savedArg.getNome());
        assertEquals("Desc A", savedArg.getDescricao());
        assertEquals(new BigDecimal("25.50"), savedArg.getValor());
        assertTrue(savedArg.getAtivo());

        verifyNoMoreInteractions(beneficioRepository);
        verifyNoInteractions(beneficioEjbService);
    }

    @Test
    void delete__shouldCallDeleteById() {
        service.delete(7L);

        verify(beneficioRepository).deleteById(7L);
        verifyNoMoreInteractions(beneficioRepository);
        verifyNoInteractions(beneficioEjbService);
    }

    @Test
    void transfer__shouldDelegateToEjbService() {
        BigDecimal amount = new BigDecimal("100.00");

        service.transfer(1L, 2L, amount);

        verify(beneficioEjbService).transfer(1L, 2L, amount);
        verifyNoInteractions(beneficioRepository);
        verifyNoMoreInteractions(beneficioEjbService);
    }
}
