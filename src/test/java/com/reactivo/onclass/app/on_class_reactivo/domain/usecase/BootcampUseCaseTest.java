package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.BootcampReport;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampReportRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import reactor.test.StepVerifier;

public class BootcampUseCaseTest {

    private BootcampRepository repository;
    private CapabilityRepository capabilityRepository;
    private TechnologyRepository technologyRepository;
    private BootcampReportRepository reportRepository;
    private BootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(BootcampRepository.class);
        capabilityRepository = Mockito.mock(CapabilityRepository.class);
        technologyRepository = Mockito.mock(TechnologyRepository.class);
        reportRepository = Mockito.mock(BootcampReportRepository.class);
        useCase = new BootcampUseCase(repository, capabilityRepository, technologyRepository, reportRepository);
    }

    @Test
    void registerBootcamp_Success() {
        Bootcamp bootcamp = Bootcamp.builder()
                .nombre("Full Stack")
                .descripcion("Desarrollo completo")
                .fechaLanzamiento(LocalDate.now())
                .duracion("10 semanas")
                .capabilityIds(List.of("1"))
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
                .capabilityIds(List.of("1", "2", "3", "4", "5")) // 5 > 4
                .build();

        StepVerifier.create(useCase.createBootcamp(bootcamp))
                .expectErrorMatches(err -> err instanceof IllegalArgumentException &&
                        err.getMessage().equals(
                                "No puede tener más de 4 capacidades asociadas."))
                .verify();
    }

    private Bootcamp bootcamp(String id, String nombre, int capabilityCount) {
        List<String> ids = IntStream.range(0, capabilityCount)
                .mapToObj(i -> "cap" + i)
                .toList();

        return Bootcamp.builder()
                .id(id)
                .nombre(nombre)
                .descripcion(null)
                .duracion("10 semanas")
                .fechaLanzamiento(LocalDate.now())
                .capabilityIds(ids)
                .build();
    }

    private Capability cap(String id, int technologies) {
        List<String> techIds = IntStream.range(0, technologies)
                .mapToObj(i -> "t" + i)
                .toList();

        return Capability.builder()
                .id(id)
                .nombre("Cap " + id)
                .descripcion(null)
                .technologyIds(techIds)
                .build();
    }

    @Test
    void listBootcamps_OrderByNameAsc() {
        Bootcamp b1 = bootcamp("1", "Angular Bootcamp", 1);
        Bootcamp b2 = bootcamp("2", "React Bootcamp", 1);

        when(repository.findAll()).thenReturn(Flux.just(b2, b1));

        // Capabilities enriquecidas (mock mínimo)
        when(capabilityRepository.findById(anyString()))
                .thenReturn(Mono.just(cap("capX", 0)));

        StepVerifier.create(useCase.getAllBootcamp("nombre", "asc", 0, 10))
                .expectNextMatches(b -> b.getNombre().equals("Angular Bootcamp"))
                .expectNextMatches(b -> b.getNombre().equals("React Bootcamp"))
                .verifyComplete();
    }

    @Test
    void listBootcamps_OrderByNameDesc() {
        Bootcamp b1 = bootcamp("1", "Angular Bootcamp", 1);
        Bootcamp b2 = bootcamp("2", "React Bootcamp", 1);

        when(repository.findAll()).thenReturn(Flux.just(b1, b2));
        when(capabilityRepository.findById(anyString()))
                .thenReturn(Mono.just(cap("capX", 0)));

        StepVerifier.create(useCase.getAllBootcamp("nombre", "desc", 0, 10))
                .expectNextMatches(b -> b.getNombre().equals("React Bootcamp"))
                .expectNextMatches(b -> b.getNombre().equals("Angular Bootcamp"))
                .verifyComplete();
    }

    @Test
    void listBootcamps_OrderByCantidadAsc() {
        Bootcamp b1 = bootcamp("1", "Bootcamp A", 1);
        Bootcamp b2 = bootcamp("2", "Bootcamp B", 2);
        Bootcamp b3 = bootcamp("3", "Bootcamp C", 3);

        when(repository.findAll()).thenReturn(Flux.just(b1, b2, b3));
        when(capabilityRepository.findById(anyString()))
                .thenReturn(Mono.just(cap("capX", 0)));

        StepVerifier.create(useCase.getAllBootcamp("cantidad", "asc", 0, 10))
                .expectNextMatches(b -> b.getCapabilityIds().size() == 1)
                .expectNextMatches(b -> b.getCapabilityIds().size() == 2)
                .expectNextMatches(b -> b.getCapabilityIds().size() == 3)
                .verifyComplete();
    }

    @Test
    void listBootcamps_OrderByCantidadDesc() {
        Bootcamp b1 = bootcamp("1", "Bootcamp A", 1);
        Bootcamp b2 = bootcamp("2", "Bootcamp B", 2);
        Bootcamp b3 = bootcamp("3", "Bootcamp C", 3);

        when(repository.findAll()).thenReturn(Flux.just(b3, b2, b1));
        when(capabilityRepository.findById(anyString()))
                .thenReturn(Mono.just(cap("capX", 0)));

        StepVerifier.create(useCase.getAllBootcamp("cantidad", "desc", 0, 10))
                .expectNextMatches(b -> b.getCapabilityIds().size() == 3)
                .expectNextMatches(b -> b.getCapabilityIds().size() == 2)
                .expectNextMatches(b -> b.getCapabilityIds().size() == 1)
                .verifyComplete();
    }

    @Test
    void listBootcamps_Pagination() {
        Bootcamp b1 = bootcamp("1", "Alpha", 1);
        Bootcamp b2 = bootcamp("2", "Beta", 1);
        Bootcamp b3 = bootcamp("3", "Gamma", 1);

        when(repository.findAll()).thenReturn(Flux.just(b1, b2, b3));
        when(capabilityRepository.findById(anyString()))
                .thenReturn(Mono.just(cap("capX", 0)));

        StepVerifier.create(useCase.getAllBootcamp("nombre", "asc", 1, 1))
                .expectNextMatches(b -> b.getNombre().equals("Beta"))
                .verifyComplete();
    }

    @Test
    void listBootcamps_MappingCapabilitiesAndTechnologies() {

        Bootcamp b = Bootcamp.builder()
                .id("b1")
                .nombre("Bootcamp Web")
                .descripcion("descripcion")
                .duracion("10 semanas")
                .fechaLanzamiento(LocalDate.now())
                .capabilityIds(List.of("cap1"))
                .build();

        Capability cap = Capability.builder()
                .id("cap1")
                .nombre("Frontend")
                .technologyIds(List.of("t1", "t2"))
                .build();

        Technology tech1 = Technology.builder().id("t1").nombre("React").build();
        Technology tech2 = Technology.builder().id("t2").nombre("Angular").build();

        when(repository.findAll()).thenReturn(Flux.just(b));
        when(capabilityRepository.findById("cap1")).thenReturn(Mono.just(cap));
        when(technologyRepository.findById("t1")).thenReturn(Mono.just(tech1));
        when(technologyRepository.findById("t2")).thenReturn(Mono.just(tech2));

        StepVerifier.create(useCase.getAllBootcamp(null, null, 0, 10))
                .assertNext(result -> {

                    Capability c = result.getCapabilities().get(0);
                    assert c.getId().equals("cap1");
                    assert c.getNombre().equals("Frontend");
                    assert c.getDescripcion() == null; // mapping correcto

                    Technology t = c.getTechnologies().get(0);
                    assert t.getId().equals("t1");
                    assert t.getNombre().equals("React");
                })
                .verifyComplete();
    }

    @Test
    void deleteBootcamp_shouldFail_whenBootcampNotFound() {
        String id = "boot1";

        Mockito.when(repository.findById(id))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteBootcamp(id))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Bootcamp no encontrado"))
                .verify();

        Mockito.verify(repository).findById(id);
        Mockito.verifyNoMoreInteractions(capabilityRepository, technologyRepository);
    }

    @Test
    void deleteBootcamp_shouldDeleteCapabilitiesNotUsedByOthers() {

        String bootcampId = "boot1";
        String capId = "cap1";

        Bootcamp b = Bootcamp.builder()
                .id(bootcampId)
                .nombre("FullStack")
                .capabilityIds(List.of(capId))
                .build();

        Capability cap = Capability.builder()
                .id(capId)
                .nombre("Frontend")
                .technologyIds(List.of())
                .build();

        Mockito.when(repository.findById(bootcampId))
                .thenReturn(Mono.just(b));

        Mockito.when(repository.countByCapabilityIdsContains(capId))
                .thenReturn(Mono.just(1L));

        Mockito.when(capabilityRepository.findById(capId))
                .thenReturn(Mono.just(cap));

        Mockito.when(capabilityRepository.deleteById(capId))
                .thenReturn(Mono.empty());

        Mockito.when(repository.deleteById(bootcampId))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteBootcamp(bootcampId))
                .verifyComplete();

        Mockito.verify(capabilityRepository).deleteById(capId);
        Mockito.verify(repository).deleteById(bootcampId);
    }

    @Test
    void deleteBootcamp_shouldNotDeleteCapabilityUsedByOthers() {

        String bootcampId = "boot1";
        String capId = "cap1";

        Bootcamp b = Bootcamp.builder()
                .id(bootcampId)
                .nombre("FullStack")
                .capabilityIds(List.of(capId))
                .build();

        Capability cap = Capability.builder()
                .id(capId)
                .nombre("Frontend")
                .technologyIds(List.of("t1"))
                .build();

        Mockito.when(repository.findById(bootcampId))
                .thenReturn(Mono.just(b));

        Mockito.when(repository.countByCapabilityIdsContains(capId))
                .thenReturn(Mono.just(2L));

        Mockito.when(capabilityRepository.findById(capId))
                .thenReturn(Mono.just(cap));

        Mockito.when(repository.deleteById(bootcampId))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteBootcamp(bootcampId))
                .verifyComplete();

        Mockito.verify(capabilityRepository, Mockito.never()).deleteById(capId);
        Mockito.verify(technologyRepository, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    void deleteBootcamp_shouldDeleteTechnologiesNotUsedByOthers() {

        String bootcampId = "boot1";
        String capId = "cap1";
        String techId = "t1";

        Bootcamp b = Bootcamp.builder()
                .id(bootcampId)
                .capabilityIds(List.of(capId))
                .build();

        Capability cap = Capability.builder()
                .id(capId)
                .technologyIds(List.of(techId))
                .build();

        Mockito.when(repository.findById(bootcampId))
                .thenReturn(Mono.just(b));

        Mockito.when(capabilityRepository.findById(capId))
                .thenReturn(Mono.just(cap));

        Mockito.when(repository.countByCapabilityIdsContains(capId))
                .thenReturn(Mono.just(1L));

        Mockito.when(capabilityRepository.countByTechnologyIdsContains(techId))
                .thenReturn(Mono.just(1L));

        Mockito.when(technologyRepository.deleteById(techId))
                .thenReturn(Mono.empty());

        Mockito.when(capabilityRepository.deleteById(capId))
                .thenReturn(Mono.empty());

        Mockito.when(repository.deleteById(bootcampId))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteBootcamp(bootcampId))
                .verifyComplete();

        Mockito.verify(technologyRepository).deleteById(techId);
        Mockito.verify(capabilityRepository).deleteById(capId);
        Mockito.verify(repository).deleteById(bootcampId);
    }

    @Test
    void deleteBootcamp_shouldNotDeleteTechnologyUsedByOthers() {

        String bootcampId = "boot1";
        String capId = "cap1";
        String techId = "t1";

        Bootcamp b = Bootcamp.builder()
                .id(bootcampId)
                .capabilityIds(List.of(capId))
                .build();

        Capability cap = Capability.builder()
                .id(capId)
                .technologyIds(List.of(techId))
                .build();

        Mockito.when(repository.findById(bootcampId))
                .thenReturn(Mono.just(b));

        Mockito.when(capabilityRepository.findById(capId))
                .thenReturn(Mono.just(cap));

        Mockito.when(repository.countByCapabilityIdsContains(capId))
                .thenReturn(Mono.just(1L));

        Mockito.when(capabilityRepository.countByTechnologyIdsContains(techId))
                .thenReturn(Mono.just(3L));

        Mockito.when(capabilityRepository.deleteById(capId))
                .thenReturn(Mono.empty());

        Mockito.when(repository.deleteById(bootcampId))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteBootcamp(bootcampId))
                .verifyComplete();

        Mockito.verify(technologyRepository, Mockito.never()).deleteById(techId);
        Mockito.verify(capabilityRepository).deleteById(capId);
    }

    @Test
    void createBootcamp_ShouldCreateReportAsync() {

        Bootcamp bootcamp = Bootcamp.builder()
                .id("b1")
                .nombre("Bootcamp Web")
                .capabilityIds(List.of("c1", "c2"))
                .build();

        Capability c1 = Capability.builder().id("c1").technologyIds(List.of("t1")).build();
        Capability c2 = Capability.builder().id("c2").technologyIds(List.of("t2", "t3")).build();

        Technology t1 = Technology.builder().id("t1").build();
        Technology t2 = Technology.builder().id("t2").build();
        Technology t3 = Technology.builder().id("t3").build();

        when(repository.existsByNombre("Bootcamp Web")).thenReturn(Mono.just(false));
        when(repository.save(any())).thenReturn(Mono.just(bootcamp));

        when(capabilityRepository.findAllById(List.of("c1", "c2")))
                .thenReturn(Flux.just(c1, c2));

        when(technologyRepository.findAllById(anyList()))
                .thenReturn(Flux.just(t1, t2, t3));

        when(reportRepository.save(any())).thenReturn(Mono.just(new BootcampReport()));

        StepVerifier.create(useCase.createBootcamp(bootcamp))
                .expectNext(bootcamp)
                .verifyComplete();

        verify(reportRepository, timeout(500).times(1)).save(any());
    }

}
