package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CapabilityUseCaseTest {

    private CapabilityRepository repository;
    private CapabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CapabilityRepository.class);
        useCase = new CapabilityUseCase(repository);
    }

    @Test
    void createCapability_shouldSaveWhenValid() {
        // given
        var techs = List.of(
                new Technology(null, "Java", "Lenguaje backend"),
                new Technology(null, "Spring Boot", "Framework reactivo"),
                new Technology(null, "MongoDB", "Base de datos NoSQL"));

        var capability = Capability.builder()
                .nombre("Backend avanzado")
                .descripcion("Capacidad con tecnologías backend")
                .tecnologias(techs)
                .build();

        Mockito.when(repository.existsByNombre("Backend avanzado")).thenReturn(Mono.just(false));
        Mockito.when(repository.save(capability)).thenReturn(Mono.just(capability));

        // when / then
        StepVerifier.create(useCase.createCapability(capability))
                .expectNext(capability)
                .verifyComplete();

        Mockito.verify(repository).existsByNombre("Backend avanzado");
        Mockito.verify(repository).save(capability);
    }

    @Test
    void createCapability_shouldFailWhenLessThan3Technologies() {
        var techs = List.of(
                new Technology(null, "Java", "Lenguaje backend"),
                new Technology(null, "Spring Boot", "Framework reactivo"));

        var capability = Capability.builder()
                .nombre("Backend intermedio")
                .descripcion("Capacidad con pocas tecnologías")
                .tecnologias(techs)
                .build();

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("Debe tener al menos 3 tecnologías asociadas"))
                .verify();

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createCapability_shouldFailWhenMoreThan20Technologies() {
        var techs = IntStream.range(0, 21)
                .mapToObj(i -> new Technology(null, "Tech" + i, "Desc" + i))
                .collect(Collectors.toList());

        var capability = Capability.builder()
                .nombre("Capacidad enorme")
                .descripcion("Demasiadas tecnologías")
                .tecnologias(techs)
                .build();

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("No puede tener más de 20 tecnologías asociadas"))
                .verify();

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createCapability_shouldFailWhenDuplicateTechnologies() {
        var techs = List.of(
                new Technology(null, "Java", "Lenguaje backend"),
                new Technology(null, "Spring Boot", "Framework reactivo"),
                new Technology(null, "Java", "Lenguaje duplicado"));

        var capability = Capability.builder()
                .nombre("Backend duplicado")
                .descripcion("Tiene tecnologías repetidas")
                .tecnologias(techs)
                .build();

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("Existen tecnologías repetidas"))
                .verify();

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createCapability_shouldFailWhenNameAlreadyExists() {
        var techs = List.of(
                new Technology(null, "Java", "Lenguaje backend"),
                new Technology(null, "Spring Boot", "Framework reactivo"),
                new Technology(null, "MongoDB", "Base de datos NoSQL"));

        var capability = Capability.builder()
                .nombre("Backend avanzado")
                .descripcion("Capacidad duplicada")
                .tecnologias(techs)
                .build();

        Mockito.when(repository.existsByNombre("Backend avanzado")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.createCapability(capability))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El nombre de la capacidad ya existe"))
                .verify();

        Mockito.verify(repository).existsByNombre("Backend avanzado");
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getAllCapabilities_shouldReturnFluxOfCapabilities() {
        var cap1 = new Capability("1", "Backend", "Capacidad backend", List.of());
        var cap2 = new Capability("2", "Frontend", "Capacidad frontend", List.of());

        Mockito.when(repository.findAll()).thenReturn(Flux.just(cap1, cap2));

        StepVerifier.create(useCase.getAllCapabilities())
                .expectNext(cap1)
                .expectNext(cap2)
                .verifyComplete();

        Mockito.verify(repository).findAll();
    }
}
