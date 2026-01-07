package com.example.backend.api.mapper;

public interface Mapper<I, O> {
    O map(I input);
}
