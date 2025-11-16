package com.reactivo.onclass.app.on_class_reactivo.domain.repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampRepository {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Flux<Bootcamp> findAll();

    Mono<Boolean> existsByNombre(String nombre);

    Mono<Bootcamp> findById(String id);

    Mono<Void> deleteById(String id);

    Mono<Long> countByCapabilityIdsContains(String capabilityId);
}
