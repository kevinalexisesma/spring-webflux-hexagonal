package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TechnologyUseCaseTest {

    private TechnologyRepository repository;
    private TechnologyUseCase useCase; 

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(TechnologyRepository.class);
        useCase = new TechnologyUseCase(repository);
    }

    @Test
    void createTechnology_shouldSaveWhenNameIsUnique() {
        // given
        Technology tech = Technology.builder()
                .nombre("Spring Boot")
                .descripcion("Framework para microservicios en Java")
                .build();

        // when
        Mockito.when(repository.existsByNombre("Spring Boot")).thenReturn(Mono.just(false));
        Mockito.when(repository.save(tech)).thenReturn(Mono.just(tech));

        // then
        StepVerifier.create(useCase.createTechnology(tech))
                .expectNext(tech)
                .verifyComplete();

        // verify repository interactions
        Mockito.verify(repository).existsByNombre("Spring Boot");
        Mockito.verify(repository).save(tech);
    }

    @Test
    void createTechnology_shouldFailWhenNameAlreadyExists() {
        // given
        Technology tech = Technology.builder()
                .nombre("Spring Boot")
                .descripcion("Framework duplicado")
                .build();

        // when
        Mockito.when(repository.existsByNombre("Spring Boot")).thenReturn(Mono.just(true));

        // then
        StepVerifier.create(useCase.createTechnology(tech))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El nombre de la tecnología ya existe"))
                .verify();

        Mockito.verify(repository).existsByNombre("Spring Boot");
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getAllTechnologies_shouldReturnFluxOfTechnologies() {
        // given
        Technology tech1 = new Technology("1", "Java", "Lenguaje de programación");
        Technology tech2 = new Technology("2", "Python", "Lenguaje interpretado");

        // when
        Mockito.when(repository.findAll()).thenReturn(Flux.just(tech1, tech2));

        // then
        StepVerifier.create(useCase.getAllTechnologies())
                .expectNext(tech1)
                .expectNext(tech2)
                .verifyComplete();

        Mockito.verify(repository).findAll();
    }
}
