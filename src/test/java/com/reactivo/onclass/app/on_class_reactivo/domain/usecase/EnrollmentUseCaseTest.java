package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Enrollment;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.EnrollmentRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class EnrollmentUseCaseTest {

    private EnrollmentRepository enrollmentRepository;
    private BootcampRepository bootcampRepository;
    private EnrollmentUseCase useCase;

    @BeforeEach
    void setUp() {
        enrollmentRepository = Mockito.mock(EnrollmentRepository.class);
        bootcampRepository = Mockito.mock(BootcampRepository.class);
        useCase = new EnrollmentUseCase(enrollmentRepository, bootcampRepository);

        // Defaults para evitar nulls
        when(enrollmentRepository.existsByPersonIdAndBootcampId(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        when(enrollmentRepository.countByPersonId(anyString()))
                .thenReturn(Mono.just(0L));

        when(enrollmentRepository.findByPersonId(anyString()))
                .thenReturn(Flux.empty());
    }

    // Helper
    private Bootcamp buildBootcamp(String id, int semanas) {
        return Bootcamp.builder()
                .id(id)
                .nombre("Bootcamp " + id)
                .duracion(semanas + " semanas")
                .fechaLanzamiento(LocalDate.of(2025, 1, 1))
                .build();
    }

    // --------------------------------------------------------
    @Test
    void enrollPerson_Success() {

        Bootcamp bootcamp = buildBootcamp("b1", 10);

        when(bootcampRepository.findById("b1"))
                .thenReturn(Mono.just(bootcamp));

        Enrollment saved = Enrollment.builder()
                .id("e1")
                .personId("p1")
                .bootcampId("b1")
                .fechaInscripcion(LocalDate.now())
                .build();

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.enrollPerson("p1", "b1"))
                .expectNext(saved)
                .verifyComplete();
    }

    // --------------------------------------------------------
    @Test
    void enrollPerson_Fails_WhenAlreadyEnrolledInBootcamp() {

        when(enrollmentRepository.existsByPersonIdAndBootcampId("p1", "b1"))
                .thenReturn(Mono.just(true));

        // NECESARIO para evitar null
        when(bootcampRepository.findById("b1"))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.enrollPerson("p1", "b1"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("La persona ya está inscrita en este bootcamp"))
                .verify();
    }

    // --------------------------------------------------------
    @Test
    void enrollPerson_Fails_WhenMoreThanFiveBootcamps() {

        when(enrollmentRepository.countByPersonId("p1"))
                .thenReturn(Mono.just(5L));

        // NECESARIO para evitar null
        when(bootcampRepository.findById("b1"))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.enrollPerson("p1", "b1"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Una persona no puede estar inscrita en más de 5 bootcamps"))
                .verify();
    }

    // --------------------------------------------------------
    @Test
    void enrollPerson_Fails_WhenBootcampNotFound() {

        when(bootcampRepository.findById("b999"))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.enrollPerson("p1", "b999"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Bootcamp no encontrado"))
                .verify();
    }

    // --------------------------------------------------------
    @Test
    void enrollPerson_Fails_WhenDatesConflict() {

        // Bootcamp nuevo (objetivo)
        Bootcamp bootNuevo = buildBootcamp("nuevo", 10);

        // Bootcamp ya inscrito por la persona
        Bootcamp bootExistente = buildBootcamp("existente", 10);

        // La persona ya tiene una inscripción
        Enrollment e1 = Enrollment.builder()
                .id("e1")
                .personId("p1")
                .bootcampId("existente")
                .build();

        when(enrollmentRepository.findByPersonId("p1"))
                .thenReturn(Flux.just(e1));

        when(bootcampRepository.findById("nuevo"))
                .thenReturn(Mono.just(bootNuevo));

        when(bootcampRepository.findById("existente"))
                .thenReturn(Mono.just(bootExistente));

        StepVerifier.create(useCase.enrollPerson("p1", "nuevo"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("Conflicto de fechas"))
                .verify();
    }

}
