package com.reactivo.onclass.app.on_class_reactivo.domain.repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyRepository {
    Mono<Technology> save(Technology technology);

    Flux<Technology> findAll();

    Mono<Boolean> existsByNombre(String nombre);
}