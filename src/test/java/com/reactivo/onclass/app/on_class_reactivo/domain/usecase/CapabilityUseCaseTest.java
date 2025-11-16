package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CapabilityUseCaseTest {

    private TechnologyRepository technologyRepository;
    private CapabilityRepository repository;
    private CapabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CapabilityRepository.class);
        technologyRepository = Mockito.mock(TechnologyRepository.class);
        useCase = new CapabilityUseCase(repository, technologyRepository);
    }

    @Test
    void createCapability_shouldSaveWhenValid() {
        // given
        var ids = List.of("1", "2", "3");

        var capability = Capability.builder()
                .nombre("Backend avanzado")
                .descripcion("Capacidad con tecnologías backend")
                .technologyIds(ids)
                .build();

        Mockito.when(repository.existsByNombre("Backend avanzado")).thenReturn(Mono.just(false));
        Mockito.when(repository.save(capability)).thenReturn(Mono.just(capability));

        StepVerifier.create(useCase.createCapability(capability))
                .expectNext(capability)
                .verifyComplete();

        Mockito.verify(repository).existsByNombre("Backend avanzado");
        Mockito.verify(repository).save(capability);
    }

    @Test
    void createCapability_shouldFailWhenLessThan3Technologies() {
        var ids = List.of("1", "2");

        var capability = Capability.builder()
                .nombre("Backend intermedio")
                .descripcion("Capacidad con pocas tecnologías")
                .technologyIds(ids)
                .build();

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage()
                                .equals("Debe tener al menos 3 tecnologías asociadas"))
                .verify();

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createCapability_shouldFailWhenMoreThan20Technologies() {
        var ids = IntStream.range(0, 21)
                .mapToObj(String::valueOf)
                .toList();

        var capability = Capability.builder()
                .nombre("Capacidad enorme")
                .descripcion("Demasiadas tecnologías")
                .technologyIds(ids)
                .build();

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals(
                                "No puede tener más de 20 tecnologías asociadas"))
                .verify();

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createCapability_shouldFailWhenDuplicateTechnologies() {
        var ids = List.of("1", "2", "1");

        var capability = Capability.builder()
                .nombre("Backend duplicado")
                .descripcion("Tiene tecnologías repetidas")
                .technologyIds(ids)
                .build();

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("Existen tecnologías repetidas"))
                .verify();

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createCapability_shouldFailWhenNameAlreadyExists() {
        var ids = List.of("1", "2", "3");

        var capability = Capability.builder()
                .nombre("Backend avanzado")
                .descripcion("Capacidad duplicada")
                .technologyIds(ids)
                .build();

        Mockito.when(repository.existsByNombre("Backend avanzado"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El nombre de la capacidad ya existe"))
                .verify();

        Mockito.verify(repository).existsByNombre("Backend avanzado");
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getAllCapabilities_shouldReturnFluxOfCapabilities() {
        var cap1 = new Capability("1", "Backend", "Capacidad backend", List.of(), List.of());
        var cap2 = new Capability("2", "Frontend", "Capacidad frontend", List.of(), List.of());

        Mockito.when(repository.findAll()).thenReturn(Flux.just(cap1, cap2));
        Mockito.when(technologyRepository.findById(Mockito.anyString()))
                .thenReturn(Mono.empty()); // no tecnologías

        StepVerifier.create(useCase.getAllCapabilities("nombre", "asc", 0, 10))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getAllCapabilities_shouldSortByNameAscending() {
        var cap1 = Capability.builder()
                .id("1").nombre("Backend").technologyIds(List.of()).build();
        var cap2 = Capability.builder()
                .id("2").nombre("Frontend").technologyIds(List.of()).build();

        Mockito.when(repository.findAll()).thenReturn(Flux.just(cap2, cap1));
        Mockito.when(technologyRepository.findById(Mockito.anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getAllCapabilities("nombre", "asc", 0, 10))
                .expectNextMatches(cap -> cap.getNombre().equals("Backend"))
                .expectNextMatches(cap -> cap.getNombre().equals("Frontend"))
                .verifyComplete();
    }

    @Test
    void getAllCapabilities_shouldSortByTechCountDescending() {
        var cap1 = Capability.builder()
                .id("1").nombre("A").technologyIds(List.of("1", "2")).build();
        var cap2 = Capability.builder()
                .id("2").nombre("B").technologyIds(List.of("3", "4", "5")).build();

        Mockito.when(repository.findAll()).thenReturn(Flux.just(cap1, cap2));
        Mockito.when(technologyRepository.findById(Mockito.anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getAllCapabilities("cantidad", "desc", 0, 10))
                .expectNextMatches(cap -> cap.getNombre().equals("B"))
                .expectNextMatches(cap -> cap.getNombre().equals("A"))
                .verifyComplete();
    }

    @Test
    void getAllCapabilities_shouldPaginateCorrectly() {
        var caps = IntStream.range(0, 6)
                .mapToObj(i -> Capability.builder()
                        .id(String.valueOf(i))
                        .nombre("Cap" + i)
                        .technologyIds(List.of())
                        .build())
                .toList();

        Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(caps));
        Mockito.when(technologyRepository.findById(Mockito.anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getAllCapabilities("nombre", "asc", 1, 2))
                .expectNextMatches(cap -> cap.getNombre().equals("Cap2"))
                .expectNextMatches(cap -> cap.getNombre().equals("Cap3"))
                .verifyComplete();
    }

}
