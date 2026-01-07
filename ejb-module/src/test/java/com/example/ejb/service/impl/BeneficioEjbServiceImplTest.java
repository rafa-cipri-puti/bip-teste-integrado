package com.example.ejb.service.impl;

import com.example.domain.Beneficio;
import com.example.ejb.exception.InsufficientBalanceException;
import com.example.ejb.exception.InvalidTransferException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioEjbServiceImplTest {

    private EntityManager em;
    private BeneficioEjbServiceImpl service;

    @BeforeEach
    void setup() throws IllegalAccessException, NoSuchFieldException {
        em = mock(EntityManager.class);
        service = new BeneficioEjbServiceImpl();
        Field field = BeneficioEjbServiceImpl.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(service, em);
    }

    @Test
    void transfer__whenFromIdOrToIdNull__shouldThrowInvalidTransfer() {
        InvalidTransferException ex1 = assertThrows(
                InvalidTransferException.class,
                () -> service.transfer(null, 2L, new BigDecimal("1.00"))
        );
        assertTrue(ex1.getMessage().contains("obrigatórios"));

        InvalidTransferException ex2 = assertThrows(
                InvalidTransferException.class,
                () -> service.transfer(1L, null, new BigDecimal("1.00"))
        );
        assertTrue(ex2.getMessage().contains("obrigatórios"));

        verifyNoInteractions(em);
    }

    @Test
    void transfer__whenSameIds__shouldThrowInvalidTransfer() {
        InvalidTransferException ex = assertThrows(
                InvalidTransferException.class,
                () -> service.transfer(1L, 1L, new BigDecimal("1.00"))
        );
        assertTrue(ex.getMessage().contains("devem ser diferentes"));

        verifyNoInteractions(em);
    }

    @Test
    void transfer__whenAmountNullOrLessOrEqualZero__shouldThrowInvalidTransfer() {
        assertThrows(InvalidTransferException.class, () -> service.transfer(1L, 2L, null));
        assertThrows(InvalidTransferException.class, () -> service.transfer(1L, 2L, BigDecimal.ZERO));
        assertThrows(InvalidTransferException.class, () -> service.transfer(1L, 2L, new BigDecimal("-1")));

        verifyNoInteractions(em);
    }

    @Test
    void transfer__whenOriginNotFound__shouldThrowEntityNotFound() {
        when(em.find(Beneficio.class, 1L, LockModeType.OPTIMISTIC)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("1.00"))
        );
        assertTrue(ex.getMessage().contains("origem"));

        verify(em).find(Beneficio.class, 1L, LockModeType.OPTIMISTIC);
        verify(em, never()).find(eq(Beneficio.class), eq(2L), any(LockModeType.class));
        verifyNoMoreInteractions(em);
    }

    @Test
    void transfer__whenDestinationNotFound__shouldThrowEntityNotFound() {
        Beneficio from = beneficioWithValor("10.00");
        when(em.find(Beneficio.class, 1L, LockModeType.OPTIMISTIC)).thenReturn(from);
        when(em.find(Beneficio.class, 2L, LockModeType.OPTIMISTIC)).thenReturn(null);

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("1.00"))
        );
        assertTrue(ex.getMessage().contains("destino"));

        verify(em).find(Beneficio.class, 1L, LockModeType.OPTIMISTIC);
        verify(em).find(Beneficio.class, 2L, LockModeType.OPTIMISTIC);
        verifyNoMoreInteractions(em);
    }

    @Test
    void transfer__whenNullValue__shouldThrowInvalidTransfer() {
        Beneficio from = new Beneficio();
        from.setValor(null);

        Beneficio to = new Beneficio();
        to.setValor(new BigDecimal("10.00"));

        when(em.find(Beneficio.class, 1L, LockModeType.OPTIMISTIC)).thenReturn(from);
        when(em.find(Beneficio.class, 2L, LockModeType.OPTIMISTIC)).thenReturn(to);

        InvalidTransferException ex = assertThrows(
                InvalidTransferException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("1.00"))
        );
        assertTrue(ex.getMessage().contains("não pode ser nulo"));

        verify(em).find(Beneficio.class, 1L, LockModeType.OPTIMISTIC);
        verify(em).find(Beneficio.class, 2L, LockModeType.OPTIMISTIC);
        verifyNoMoreInteractions(em);
    }

    @Test
    void transfer__whenInsufficientBalance__shouldThrowInsufficientBalance() {
        Beneficio from = beneficioWithValor("5.00");
        Beneficio to = beneficioWithValor("10.00");

        when(em.find(Beneficio.class, 1L, LockModeType.OPTIMISTIC)).thenReturn(from);
        when(em.find(Beneficio.class, 2L, LockModeType.OPTIMISTIC)).thenReturn(to);

        assertThrows(
                InsufficientBalanceException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("6.00"))
        );

        assertEquals(new BigDecimal("5.00"), from.getValor());
        assertEquals(new BigDecimal("10.00"), to.getValor());

        verify(em).find(Beneficio.class, 1L, LockModeType.OPTIMISTIC);
        verify(em).find(Beneficio.class, 2L, LockModeType.OPTIMISTIC);
        verify(em, never()).lock(any(), any());
        verify(em, never()).flush();
        verifyNoMoreInteractions(em);
    }

    @Test
    void transfer__whenOk__shouldDebit__lockEFlush() {
        Beneficio from = beneficioWithValor("100.00");
        Beneficio to = beneficioWithValor("30.00");

        when(em.find(Beneficio.class, 1L, LockModeType.OPTIMISTIC)).thenReturn(from);
        when(em.find(Beneficio.class, 2L, LockModeType.OPTIMISTIC)).thenReturn(to);

        service.transfer(1L, 2L, new BigDecimal("25.50"));

        assertEquals(new BigDecimal("74.50"), from.getValor());
        assertEquals(new BigDecimal("55.50"), to.getValor());

        verify(em).find(Beneficio.class, 1L, LockModeType.OPTIMISTIC);
        verify(em).find(Beneficio.class, 2L, LockModeType.OPTIMISTIC);
        verify(em).lock(from, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        verify(em).lock(to, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        verify(em).flush();
        verifyNoMoreInteractions(em);
    }

    private static Beneficio beneficioWithValor(String valor) {
        Beneficio b = new Beneficio();
        b.setValor(new BigDecimal(valor));
        return b;
    }
}
