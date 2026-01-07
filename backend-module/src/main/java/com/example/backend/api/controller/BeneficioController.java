package com.example.backend.api.controller;

import com.example.backend.api.dto.request.BeneficioRequestDto;
import com.example.backend.api.dto.request.TransferRequestDto;
import com.example.backend.api.dto.response.BeneficioResponseDto;
import com.example.backend.api.dto.response.TransferResponseDto;
import com.example.backend.api.mapper.impl.BeneficioRequestDtoMapper;
import com.example.backend.api.mapper.impl.BeneficioResponseDtoMapper;
import com.example.backend.service.BeneficioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {
    private final BeneficioService beneficioService;
    private final BeneficioResponseDtoMapper beneficioResponseDtoMapper = new BeneficioResponseDtoMapper();
    private final BeneficioRequestDtoMapper beneficioRequestDtoMapper = new BeneficioRequestDtoMapper();

    @Autowired
    public BeneficioController(BeneficioService beneficioService) {
        this.beneficioService = beneficioService;
    }

    @GetMapping
    public ResponseEntity<Page<BeneficioResponseDto>> getAll(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(beneficioService.getAll(pageable).map(beneficioResponseDtoMapper::map));
    }

    @PostMapping
    public ResponseEntity<BeneficioResponseDto> create(@RequestBody @Valid BeneficioRequestDto beneficioRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficioResponseDtoMapper.map(beneficioService.create(beneficioRequestDtoMapper.map(beneficioRequestDto))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeneficioResponseDto> update(@PathVariable("id") Long id, @RequestBody @Valid BeneficioRequestDto beneficioRequestDto) {
        return ResponseEntity.ok(beneficioResponseDtoMapper.map(beneficioService.update(id, beneficioRequestDtoMapper.map(beneficioRequestDto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        beneficioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(@RequestBody @Valid TransferRequestDto transferRequestDto) {
        beneficioService.transfer(transferRequestDto.getFromId(), transferRequestDto.getToId(), transferRequestDto.getAmount());
        return ResponseEntity.ok(TransferResponseDto.builder()
                .fromBeneficio(beneficioResponseDtoMapper.map(beneficioService.getById(transferRequestDto.getFromId())))
                .toBeneficio(beneficioResponseDtoMapper.map(beneficioService.getById(transferRequestDto.getToId())))
                .build());
    }
}
