package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;

import reactor.core.publisher.Mono;

public interface MongoCapabilityRepository extends ReactiveMongoRepository<Capability, String> {

    Mono<Boolean> existsByNombre(String nombre);
}
