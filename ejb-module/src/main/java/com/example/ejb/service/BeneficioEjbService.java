package com.example.ejb.service;

import jakarta.ejb.Remote;

import java.math.BigDecimal;

@Remote
public interface BeneficioEjbService {
    void transfer(Long fromId, Long toId, BigDecimal amount);
}
