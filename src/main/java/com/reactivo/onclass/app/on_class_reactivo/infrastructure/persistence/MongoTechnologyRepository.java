package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MongoTechnologyRepository extends ReactiveMongoRepository<Technology, String> {
    Mono<Boolean> existsByNombre(String nombre);

    Flux<Technology> findAllById(Iterable<String> ids);
}
