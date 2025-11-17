package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Enrollment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MongoEnrollmentRepository extends ReactiveMongoRepository<Enrollment, String> {

    Flux<Enrollment> findByPersonId(String personId);

    Mono<Long> countByPersonId(String personId);

    Mono<Boolean> existsByPersonIdAndBootcampId(String personId, String bootcampId);

    Mono<Long> countByBootcampId(String bootcampId);

    Flux<Enrollment> findByBootcampId(String bootcampId);
}
