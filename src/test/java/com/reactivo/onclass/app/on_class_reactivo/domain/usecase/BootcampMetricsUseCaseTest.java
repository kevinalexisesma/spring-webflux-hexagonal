package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.reactivo.onclass.app.on_class_reactivo.application.dto.CapabilityDetailDTO;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Enrollment;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Person;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.EnrollmentRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.PersonRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class BootcampMetricsUseCaseTest {

    private BootcampRepository bootcampRepository;
    private EnrollmentRepository enrollmentRepository;
    private PersonRepository personRepository;
    private CapabilityRepository capabilityRepository;
    private TechnologyRepository technologyRepository;
    private BootcampMetricsUseCase useCase;

    @BeforeEach
    void setUp() {
        bootcampRepository = Mockito.mock(BootcampRepository.class);
        enrollmentRepository = Mockito.mock(EnrollmentRepository.class);
        personRepository = Mockito.mock(PersonRepository.class);
        capabilityRepository = Mockito.mock(CapabilityRepository.class);
        technologyRepository = Mockito.mock(TechnologyRepository.class);
        useCase = new BootcampMetricsUseCase(bootcampRepository,
                enrollmentRepository,
                capabilityRepository,
                technologyRepository,
                personRepository);
    }

    @Test
    void shouldReturnBootcampWithMostEnrollments() {

        // ---------- BOOTCAMPS ----------
        Bootcamp boot1 = Bootcamp.builder()
                .id("b1")
                .nombre("Backend")
                .descripcion("desc")
                .capabilityIds(List.of("c1"))
                .build();

        Bootcamp boot2 = Bootcamp.builder()
                .id("b2")
                .nombre("Frontend")
                .descripcion("desc")
                .capabilityIds(List.of("c1", "c2"))
                .build();

        when(bootcampRepository.findAll())
                .thenReturn(Flux.just(boot1, boot2));

        // ---------- ENROLLMENT COUNTS ----------
        when(enrollmentRepository.countByBootcampId("b1")).thenReturn(Mono.just(10L));
        when(enrollmentRepository.countByBootcampId("b2")).thenReturn(Mono.just(15L));

        // ---------- CAPABILITIES OF BOOT2 ----------
        Capability cap1 = Capability.builder()
                .id("c1")
                .nombre("Java")
                .descripcion("desc")
                .technologyIds(List.of("t1"))
                .build();

        Capability cap2 = Capability.builder()
                .id("c2")
                .nombre("React")
                .descripcion("desc")
                .technologyIds(List.of("t2"))
                .build();

        when(capabilityRepository.findAllById(List.of("c1", "c2")))
                .thenReturn(Flux.just(cap1, cap2));

        // fallback matcher (evita errores si el iterable difiere)
        when(capabilityRepository.findAllById(anyList()))
                .thenReturn(Flux.just(cap1, cap2));

        // ---------- TECHNOLOGIES ----------
        Technology t1 = Technology.builder().id("t1").nombre("Spring").descripcion("d").build();
        Technology t2 = Technology.builder().id("t2").nombre("JavaScript").descripcion("d").build();

        when(technologyRepository.findAllById(List.of("t1"))).thenReturn(Flux.just(t1));
        when(technologyRepository.findAllById(List.of("t2"))).thenReturn(Flux.just(t2));

        // fallback matcher
        when(technologyRepository.findAllById(anyList()))
                .thenReturn(Flux.just(t1, t2));

        // ---------- ENROLLMENTS FOR BOOT2 ----------
        Enrollment e1 = Enrollment.builder().personId("p1").bootcampId("b2").build();
        Enrollment e2 = Enrollment.builder().personId("p2").bootcampId("b2").build();

        when(enrollmentRepository.findByBootcampId("b2"))
                .thenReturn(Flux.just(e1, e2));

        // ---------- PERSONS ----------
        when(personRepository.findById("p1"))
                .thenReturn(Mono.just(new Person("p1", "Carlos", "carlos@mail.com")));
        when(personRepository.findById("p2"))
                .thenReturn(Mono.just(new Person("p2", "Maria", "maria@mail.com")));

        // ---------- EXECUTE ----------
        StepVerifier.create(useCase.getBootcampWithMostEnrollments())
                .assertNext(result -> {
                    Assertions.assertEquals("b2", result.getId());
                    Assertions.assertEquals(2, result.getPersonasInscritas().size());
                    Assertions.assertEquals(2, result.getCapacidades().size());

                    CapabilityDetailDTO capDto = result.getCapacidades().get(0);
                    Assertions.assertNotNull(capDto.getId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFail_WhenNoBootcampsExist() {
        when(bootcampRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.getBootcampWithMostEnrollments())
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().contains("No existen bootcamps"))
                .verify();
    }
}
