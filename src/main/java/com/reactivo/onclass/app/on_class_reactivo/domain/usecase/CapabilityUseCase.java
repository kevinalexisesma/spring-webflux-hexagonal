package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import java.util.HashSet;
import java.util.List;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CapabilityUseCase {

    private final CapabilityRepository repository;
    private final TechnologyRepository technologyRepository;

    public CapabilityUseCase(CapabilityRepository repository, TechnologyRepository technologyRepository) {
        this.repository = repository;
        this.technologyRepository = technologyRepository;
    }

    public Mono<Capability> createCapability(Capability capability) {
        List<String> technologyIds = capability.getTechnologyIds();

        if (technologyIds == null || technologyIds.size() < 3) {
            return Mono.error(new IllegalArgumentException("Debe tener al menos 3 tecnologías asociadas"));
        }

        if (technologyIds.size() > 20) {
            return Mono.error(new IllegalArgumentException("No puede tener más de 20 tecnologías asociadas"));
        }

        var idsSet = new HashSet<String>();
        for (String techId : technologyIds) {
            if (!idsSet.add(techId)) {
                return Mono.error(new IllegalArgumentException("Existen tecnologías repetidas"));
            }
        }

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
                    int comparison;
                    if ("cantidad".equalsIgnoreCase(sortBy)) {
                        int s1 = c1.getTechnologyIds() == null ? 0 : c1.getTechnologyIds().size();
                        int s2 = c2.getTechnologyIds() == null ? 0 : c2.getTechnologyIds().size();
                        comparison = Integer.compare(s1, s2);
                    } else { 
                        comparison = c1.getNombre().compareToIgnoreCase(c2.getNombre());
                    }
                    return "desc".equalsIgnoreCase(order) ? -comparison : comparison;
                })
                .skip((long) page * size)
                .take(size)
                .flatMap(cap -> {
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
                });
    }

}
