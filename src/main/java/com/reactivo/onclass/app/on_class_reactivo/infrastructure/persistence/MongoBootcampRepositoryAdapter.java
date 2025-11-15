package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MongoBootcampRepositoryAdapter implements BootcampRepository {

    private final MongoBootcampRepository repository;

    public MongoBootcampRepositoryAdapter(MongoBootcampRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return repository.save(bootcamp);
    }

    @Override
    public Flux<Bootcamp> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return repository.existsByNombre(nombre);
    }

    @Override
    public Mono<Bootcamp> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

}
