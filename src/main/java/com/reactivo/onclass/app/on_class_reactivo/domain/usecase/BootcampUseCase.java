package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;


import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;

import reactor.core.publisher.Mono;

public class BootcampUseCase {

    private final BootcampRepository repository;

    public BootcampUseCase(BootcampRepository repository) {
        this.repository = repository;
    }

    public Mono<Bootcamp> createBootcamp(Bootcamp bootcamp) {

        if (bootcamp.getCapacidades() == null || bootcamp.getCapacidades().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Debe incluir al menos una capacidad"));
        }

        if (bootcamp.getCapacidades().size() > 4) {
            return Mono.error(new IllegalArgumentException("No puede tener más de 4 capacidades asociada."));
        }

        return repository.existsByNombre(bootcamp.getNombre())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("El nombre del bootcamp ya existe."));
                    }
                    return repository.save(bootcamp);
                });
    }
}
