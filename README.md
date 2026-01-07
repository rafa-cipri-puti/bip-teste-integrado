# 📦 Benefícios – Sistema de Gestão e Transferência

Este projeto implementa um sistema completo de **gestão de benefícios** com suporte a **transferência de valores**, utilizando uma **arquitetura distribuída** baseada em **Spring Boot + EJB (WildFly)**, banco **PostgreSQL**, frontend **Angular 19**, e testes **end-to-end via Docker**.

---

## 🧠 Visão Geral da Arquitetura

O sistema é composto por **quatro camadas principais**, cada uma com responsabilidades bem definidas:

```
┌─────────────┐
│  Angular 19 │  → Frontend SPA
└──────┬──────┘
       │ HTTP/JSON
┌──────▼──────┐
│ Spring Boot │  → API REST, orquestração, validações
└──────┬──────┘
       │ JNDI (EJB Client)
┌──────▼──────┐
│   WildFly   │  → Regras críticas de negócio (EJB)
└──────┬──────┘
       │ JPA
┌──────▼──────┐
│ PostgreSQL  │
└─────────────┘
```

## ▶️ Execução rápida

```bash
docker compose up -d --build
```

Frontend: http://localhost:4200  
Backend: http://localhost:8080  
Swagger: http://localhost:8080/swagger-ui/index.html  

---

## 🧪 Testes E2E

```bash
chmod +x e2e.sh
./e2e.sh
```
