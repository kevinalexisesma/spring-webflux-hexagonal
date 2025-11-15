package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class MongoCapabilityRepositoryAdapter implements CapabilityRepository {

    private final MongoCapabilityRepository repository;

    public MongoCapabilityRepositoryAdapter(MongoCapabilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Capability> save(Capability capability) {
        return repository.save(capability);
    }

    @Override
    public Flux<Capability> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return repository.existsByNombre(nombre);
    }

    @Override
    public Mono<Capability> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Long> countByTechnologyIdsContains(String technologyId) {
        return repository.countByTechnologyIdsContains(technologyId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }
}
