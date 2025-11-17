package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MongoCapabilityRepository extends ReactiveMongoRepository<Capability, String> {

    Mono<Boolean> existsByNombre(String nombre);

    Mono<Long> countByTechnologyIdsContains(String technologyId);

    Flux<Capability> findAllById(Iterable<String> ids);
}
