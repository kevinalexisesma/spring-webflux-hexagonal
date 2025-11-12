package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TechnologyUseCase {

    private final TechnologyRepository repository;

    public TechnologyUseCase(TechnologyRepository repository) {
        this.repository = repository;
    }

    public Mono<Technology> createTechnology(Technology technology) {
        return repository.existsByNombre(technology.getNombre())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("El nombre de la tecnología ya existe"));
                    }
                    return repository.save(technology);
                });
    }

    public Flux<Technology> getAllTechnologies() {
        return repository.findAll();
    }
}
