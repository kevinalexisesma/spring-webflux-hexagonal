package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import java.util.HashSet;
import java.util.List;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CapabilityUseCase {

    private final CapabilityRepository repository;

    public CapabilityUseCase(CapabilityRepository repository) {
        this.repository = repository;
    }

    public Mono<Capability> createCapability(Capability capability) {
        List<Technology> tecnologias = capability.getTecnologias();

        if (tecnologias == null || tecnologias.size() < 3) {
            return Mono.error(new IllegalArgumentException("Debe tener al menos 3 tecnologías asociadas"));
        }

        if (tecnologias.size() > 20) {
            return Mono.error(new IllegalArgumentException("No puede tener más de 20 tecnologías asociadas"));
        }

        // Validar duplicados
        var nombres = new HashSet<String>();
        for (Technology tech : tecnologias) {
            if (!nombres.add(tech.getNombre())) {
                return Mono.error(new IllegalArgumentException("Existen tecnologías repetidas"));
            }
        }

        // Verificar nombre único
        return repository.existsByNombre(capability.getNombre())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("El nombre de la capacidad ya existe"));
                    }
                    return repository.save(capability);
                });
    }

    public Flux<Capability> getAllCapabilities() {
        return repository.findAll();
    }

    public Flux<Capability> getAllCapabilities(
            String sortBy, String order, int page, int size) {

        return repository.findAll()
                .sort((c1, c2) -> {
                    // Ordenar según parámetro
                    int comparison;
                    if ("cantidad".equalsIgnoreCase(sortBy)) {
                        comparison = Integer.compare(
                                c1.getTecnologias().size(),
                                c2.getTecnologias().size());
                    } else { // por nombre
                        comparison = c1.getNombre().compareToIgnoreCase(c2.getNombre());
                    }
                    // Aplicar orden descendente si se pide
                    return "desc".equalsIgnoreCase(order) ? -comparison : comparison;
                })
                .skip((long) page * size)
                .take(size)
                .map(cap -> {
                    // Solo devolver tecnologías con id y nombre
                    var reducedTechs = cap.getTecnologias().stream()
                            .map(t -> new Technology(t.getId(), t.getNombre(), null))
                            .toList();
                    cap.setTecnologias(reducedTechs);
                    return cap;
                });
    }

}
