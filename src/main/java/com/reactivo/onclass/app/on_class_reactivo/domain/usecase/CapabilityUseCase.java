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
}
