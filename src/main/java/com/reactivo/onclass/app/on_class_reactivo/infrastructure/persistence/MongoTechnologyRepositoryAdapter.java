package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MongoTechnologyRepositoryAdapter implements TechnologyRepository {

    private final MongoTechnologyRepository repository;

    public MongoTechnologyRepositoryAdapter(MongoTechnologyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Technology> save(Technology technology) {
        return repository.save(technology);
    }

    @Override
    public Flux<Technology> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return repository.existsByNombre(nombre);
    }
}