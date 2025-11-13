package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;

import reactor.core.publisher.Mono;

public interface MongoBootcampRepository extends ReactiveMongoRepository<Bootcamp, String> {

    Mono<Boolean> existsByNombre(String nombre);
}
