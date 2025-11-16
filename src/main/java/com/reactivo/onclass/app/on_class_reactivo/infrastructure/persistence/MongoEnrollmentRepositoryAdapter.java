package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Enrollment;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.EnrollmentRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MongoEnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final MongoEnrollmentRepository repository;

    public MongoEnrollmentRepositoryAdapter(MongoEnrollmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByPersonIdAndBootcampId(String personId, String bootcampId) {
        return repository.existsByPersonIdAndBootcampId(personId, bootcampId);
    }

    @Override
    public Mono<Long> countByPersonId(String personId) {
        return repository.countByPersonId(personId);
    }

    @Override
    public Flux<Enrollment> findByPersonId(String personId) {
        return repository.findByPersonId(personId);
    }

    @Override
    public Mono<Enrollment> save(Enrollment enrollment) {
        return repository.save(enrollment);
    }

}
