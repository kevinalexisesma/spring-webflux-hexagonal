package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;

import reactor.core.publisher.Flux;
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

    public Flux<Bootcamp> getAllBootcamp(String sortBy, String order, int page, int size) {
        return repository.findAll()
                .sort((b1, b2) -> {
                    int comparasion;
                    if ("cantidad".equalsIgnoreCase(sortBy)) {
                        comparasion = Integer.compare(
                                b1.getCapacidades().size(),
                                b2.getCapacidades().size());
                    } else {
                        comparasion = b1.getNombre().compareToIgnoreCase(b2.getNombre());
                    }

                    return "desc".equalsIgnoreCase(order) ? -comparasion : comparasion;
                })
                .skip((long) page * size)
                .take(size)
                .map(boot -> {
                    var capacidades = boot.getCapacidades().stream()
                            .map(cap -> {
                                var tecnologias = cap.getTecnologias().stream()
                                        .map(tecno -> {
                                            return new Technology(tecno.getId(), tecno.getNombre(), null);
                                        })
                                        .toList();
                                return new Capability(cap.getId(), cap.getNombre(), null, tecnologias);
                            })
                            .toList();
                    boot.setCapacidades(capacidades);
                    return boot;
                });
    }
}
