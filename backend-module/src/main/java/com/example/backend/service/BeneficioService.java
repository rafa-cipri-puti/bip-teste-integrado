package com.example.backend.service;

import com.example.backend.infrastructure.repository.BeneficioRepository;
import com.example.domain.Beneficio;
import com.example.ejb.service.BeneficioEjbService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class BeneficioService {
    private final BeneficioEjbService beneficioEjbService;
    private final BeneficioRepository beneficioRepository;

    @Autowired
    public BeneficioService(BeneficioEjbService beneficioEjbService, BeneficioRepository beneficioRepository) {
        this.beneficioEjbService = beneficioEjbService;
        this.beneficioRepository = beneficioRepository;
    }

    public Page<Beneficio> getAll(Pageable pageable) {
        return beneficioRepository.findAll(pageable);
    }

    public Beneficio getById(Long id) {
        return beneficioRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public Beneficio create(Beneficio beneficio) {
        return beneficioRepository.save(beneficio);
    }

    @Transactional
    public Beneficio update(Long id, Beneficio updatedBeneficio) {
        final Beneficio actual = beneficioRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        replaceIfNotNull(actual, updatedBeneficio);
        return beneficioRepository.save(actual);
    }

    @Transactional
    public void delete(Long id) {
        beneficioRepository.deleteById(id);
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        beneficioEjbService.transfer(fromId, toId, amount);
    }

    private void replaceIfNotNull(Beneficio beneficio, Beneficio updatedBeneficio) {
        if (Objects.nonNull(updatedBeneficio.getNome())) {
            beneficio.setNome(updatedBeneficio.getNome());
        }
        if (Objects.nonNull(updatedBeneficio.getDescricao())) {
            beneficio.setDescricao(updatedBeneficio.getDescricao());
        }
        if (Objects.nonNull(updatedBeneficio.getValor())) {
            beneficio.setValor(updatedBeneficio.getValor());
        }
        if (Objects.nonNull(updatedBeneficio.getAtivo())) {
            beneficio.setAtivo(updatedBeneficio.getAtivo());
        }
    }
}
