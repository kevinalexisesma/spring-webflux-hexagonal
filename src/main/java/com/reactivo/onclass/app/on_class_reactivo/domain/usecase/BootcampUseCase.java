package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BootcampUseCase {

    private final BootcampRepository repository;
    private final CapabilityRepository capabilityRepository;
    private final TechnologyRepository technologyRepository;

    public BootcampUseCase(BootcampRepository repository, CapabilityRepository capabilityRepository,
            TechnologyRepository technologyRepository) {
        this.repository = repository;
        this.capabilityRepository = capabilityRepository;
        this.technologyRepository = technologyRepository;
    }

    public Mono<Bootcamp> createBootcamp(Bootcamp bootcamp) {

        if (bootcamp.getCapabilityIds() == null || bootcamp.getCapabilityIds().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Debe incluir al menos una capacidad"));
        }

        if (bootcamp.getCapabilityIds().size() > 4) {
            return Mono.error(new IllegalArgumentException("No puede tener más de 4 capacidades asociadas."));
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
                    int comparison;

                    if ("cantidad".equalsIgnoreCase(sortBy)) {
                        int s1 = b1.getCapabilityIds() == null ? 0 : b1.getCapabilityIds().size();
                        int s2 = b2.getCapabilityIds() == null ? 0 : b2.getCapabilityIds().size();
                        comparison = Integer.compare(s1, s2);
                    } else {
                        comparison = b1.getNombre().compareToIgnoreCase(b2.getNombre());
                    }

                    return "desc".equalsIgnoreCase(order) ? -comparison : comparison;
                })
                .skip((long) page * size)
                .take(size)
                .flatMap(this::enrichBootcamp);
    }

    private Mono<Bootcamp> enrichBootcamp(Bootcamp bootcamp) {

        if (bootcamp.getCapabilityIds() == null || bootcamp.getCapabilityIds().isEmpty()) {
            bootcamp.setCapabilities(List.of());
            return Mono.just(bootcamp);
        }

        return Flux.fromIterable(bootcamp.getCapabilityIds())
                .flatMap(capId -> capabilityRepository.findById(capId))
                .flatMap(this::enrichCapability)
                .collectList()
                .map(capList -> {
                    bootcamp.setCapabilities(capList);
                    return bootcamp;
                });
    }

    private Mono<Capability> enrichCapability(Capability cap) {

        if (cap.getTechnologyIds() == null || cap.getTechnologyIds().isEmpty()) {
            cap.setTechnologies(List.of());
            return Mono.just(cap);
        }

        return Flux.fromIterable(cap.getTechnologyIds())
                .flatMap(techId -> technologyRepository.findById(techId))
                .map(tech -> Technology.builder()
                        .id(tech.getId())
                        .nombre(tech.getNombre())
                        .descripcion(null)
                        .build())
                .collectList()
                .map(techList -> {
                    cap.setTechnologies(techList);
                    return cap;
                });
    }

}
