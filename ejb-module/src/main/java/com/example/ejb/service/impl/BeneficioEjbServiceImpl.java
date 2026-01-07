package com.example.ejb.service.impl;

import com.example.domain.Beneficio;
import com.example.ejb.exception.InsufficientBalanceException;
import com.example.ejb.exception.InvalidTransferException;
import com.example.ejb.service.BeneficioEjbService;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;

@Stateless(name = "BeneficioEjbServiceImpl")
public class BeneficioEjbServiceImpl implements BeneficioEjbService {

    @PersistenceContext(unitName = "appPU")
    private EntityManager em;

    @Override
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId == null || toId == null) {
            throw new InvalidTransferException("fromId e toId são obrigatórios");
        }
        if (fromId.equals(toId)) {
            throw new InvalidTransferException("fromId e toId devem ser diferentes");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("amount deve ser maior que zero");
        }

        Beneficio from = em.find(Beneficio.class, fromId, LockModeType.OPTIMISTIC);
        if (from == null) {
            throw new EntityNotFoundException("Beneficio origem não encontrado: " + fromId);
        }

        Beneficio to = em.find(Beneficio.class, toId, LockModeType.OPTIMISTIC);
        if (to == null) {
            throw new EntityNotFoundException("Beneficio destino não encontrado: " + toId);
        }

        if (from.getValor() == null || to.getValor() == null) {
            throw new InvalidTransferException("Valor não pode ser nulo");
        }

        if (from.getValor().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        from.setValor(from.getValor().subtract(amount));
        to.setValor(to.getValor().add(amount));

        em.lock(from, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        em.lock(to, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        em.flush();
    }
}
