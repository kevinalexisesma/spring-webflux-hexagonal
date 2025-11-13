package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class BootcampUseCaseTest {

    @Mock
    private BootcampRepository repository;

    private BootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new BootcampUseCase(repository);
    }

    @Test
    void registerBootcamp_Success() {
        Bootcamp bootcamp = Bootcamp.builder()
                .nombre("Full Stack")
                .descripcion("Desarrollo completo")
                .fechaLanzamiento(LocalDate.now())
                .duracion("10 semanas")
                .capacidades(List.of(new Capability("1", "Frontend", "HTML/CSS/JS", List.of())))
                .build();

        when(repository.existsByNombre("Full Stack")).thenReturn(Mono.just(false));
        when(repository.save(any(Bootcamp.class))).thenReturn(Mono.just(bootcamp));

        StepVerifier.create(useCase.createBootcamp(bootcamp))
                .expectNext(bootcamp)
                .verifyComplete();
    }

    @Test
    void registerBootcamp_WithTooManyCapabilities_Fails() {
        Bootcamp bootcamp = Bootcamp.builder()
                .nombre("Exceso Bootcamp")
                .descripcion("Prueba")
                .fechaLanzamiento(LocalDate.now())
                .duracion("8 semanas")
                .capacidades(List.of(
                        new Capability(), new Capability(),
                        new Capability(), new Capability(), new Capability()))
                .build();

        StepVerifier.create(useCase.createBootcamp(bootcamp))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
