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
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;

import reactor.core.publisher.Flux;
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

    private Bootcamp bootcamp(String id, String nombre, int capacidadesCount) {
        return Bootcamp.builder()
                .id(id)
                .nombre(nombre)
                .descripcion(null)
                .duracion("10 semanas")
                .fechaLanzamiento(LocalDate.now())
                .capacidades(
                        capacidadesCount == 0
                                ? List.of()
                                : List.of(cap("c1", capacidadesCount)))
                .build();
    }

    private Capability cap(String id, int technologies) {
        return Capability.builder()
                .id(id)
                .nombre("Cap " + id)
                .descripcion(null)
                .tecnologias(
                        technologies == 0
                                ? List.of()
                                : List.of(Technology.builder().id("t1").nombre("Tec 1")
                                        .build()))
                .build();
    }

    @Test
    void listBootcamps_OrderByNameAsc() {
        Bootcamp b1 = bootcamp("1", "Angular Bootcamp", 1);
        Bootcamp b2 = bootcamp("2", "React Bootcamp", 1);

        when(repository.findAll()).thenReturn(Flux.just(b2, b1));

        StepVerifier.create(useCase.getAllBootcamp("nombre", "asc", 0, 10))
                .expectNext(b1)
                .expectNext(b2)
                .verifyComplete();
    }

    @Test
    void listBootcamps_OrderByNameDesc() {
        Bootcamp b1 = bootcamp("1", "Angular Bootcamp", 1);
        Bootcamp b2 = bootcamp("2", "React Bootcamp", 1);

        when(repository.findAll()).thenReturn(Flux.just(b1, b2));

        StepVerifier.create(useCase.getAllBootcamp("nombre", "desc", 0, 10))
                .expectNext(b2)
                .expectNext(b1)
                .verifyComplete();
    }

    @Test
    void listBootcamps_OrderByCantidadAsc() {
        Bootcamp b1 = bootcamp("1", "Bootcamp A", 1);
        Bootcamp b2 = bootcamp("2", "Bootcamp B", 2);
        Bootcamp b3 = bootcamp("3", "Bootcamp C", 3);

        when(repository.findAll()).thenReturn(Flux.just(b1, b2, b3));

        StepVerifier.create(useCase.getAllBootcamp("cantidad", "asc", 0, 10))
                .expectNext(b1) // 1 capacidad
                .expectNext(b2) // 2 capacidades
                .expectNext(b3) // 3 capacidades
                .verifyComplete();
    }

    @Test
    void listBootcamps_OrderByCantidadDesc() {
        Bootcamp b1 = bootcamp("1", "Bootcamp A", 1);
        Bootcamp b2 = bootcamp("2", "Bootcamp B", 2);
        Bootcamp b3 = bootcamp("3", "Bootcamp C", 3);

        when(repository.findAll()).thenReturn(Flux.just(b3, b2, b1));

        StepVerifier.create(useCase.getAllBootcamp("cantidad", "desc", 0, 10))
                .expectNext(b3) // 3 capacidades
                .expectNext(b2) // 2
                .expectNext(b1) // 1
                .verifyComplete();
    }

    @Test
    void listBootcamps_Pagination() {
        Bootcamp b1 = bootcamp("1", "Alpha", 1);
        Bootcamp b2 = bootcamp("2", "Beta", 1);
        Bootcamp b3 = bootcamp("3", "Gamma", 1);

        when(repository.findAll()).thenReturn(Flux.just(b1, b2, b3));

        StepVerifier.create(useCase.getAllBootcamp("nombre", "asc", 1, 1))
                .expectNext(b2) // página 1, size 1 => segundo elemento
                .verifyComplete();
    }

    @Test
    void listBootcamps_MappingCapabilitiesAndTechnologies() {
        Capability cap = Capability.builder()
                .id("cap1")
                .nombre("Frontend")
                .descripcion("No debe venir")
                .tecnologias(List.of(
                        Technology.builder().id("t1").nombre("React").build(),
                        Technology.builder().id("t2").nombre("Angular").build()))
                .build();

        Bootcamp b = Bootcamp.builder()
                .id("b1")
                .nombre("Bootcamp Web")
                .descripcion("descripcion")
                .duracion("10 semanas")
                .fechaLanzamiento(LocalDate.now())
                .capacidades(List.of(cap))
                .build();

        when(repository.findAll()).thenReturn(Flux.just(b));

        StepVerifier.create(useCase.getAllBootcamp(null, null, 0, 10))
                .assertNext(result -> {
                    Capability c = result.getCapacidades().get(0);
                    assert c.getId().equals("cap1");
                    assert c.getNombre().equals("Frontend");

                    // description debe haber sido eliminado en el mapeo parcial
                    assert c.getDescripcion() == null;

                    Technology t = c.getTecnologias().get(0);
                    assert t.getId().equals("t1");
                    assert t.getNombre().equals("React");
                })
                .verifyComplete();
    }
}
