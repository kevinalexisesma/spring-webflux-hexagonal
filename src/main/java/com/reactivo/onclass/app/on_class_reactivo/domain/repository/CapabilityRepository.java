package com.reactivo.onclass.app.on_class_reactivo.domain.repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CapabilityRepository {

    Mono<Capability> save(Capability capability);

    Flux<Capability> findAll();

    Mono<Boolean> existsByNombre(String nombre);
}
