package com.reactivo.onclass.app.on_class_reactivo.domain.repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Enrollment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnrollmentRepository {

    Mono<Boolean> existsByPersonIdAndBootcampId(String personId, String bootcampId);

    Mono<Long> countByPersonId(String personId);

    Flux<Enrollment> findByPersonId(String personId);

    Mono<Enrollment> save(Enrollment enrollment);

}