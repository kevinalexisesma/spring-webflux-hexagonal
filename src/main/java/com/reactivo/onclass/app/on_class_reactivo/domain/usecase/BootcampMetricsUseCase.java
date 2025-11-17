package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import java.util.List;

import com.reactivo.onclass.app.on_class_reactivo.application.dto.BootcampDetailDTO;
import com.reactivo.onclass.app.on_class_reactivo.application.dto.CapabilityDetailDTO;
import com.reactivo.onclass.app.on_class_reactivo.application.dto.PersonDTO;
import com.reactivo.onclass.app.on_class_reactivo.application.dto.TechnologyDTO;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.EnrollmentRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.PersonRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class BootcampMetricsUseCase {

    private final BootcampRepository bootcampRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CapabilityRepository capabilityRepository;
    private final TechnologyRepository technologyRepository;
    private final PersonRepository personRepository;

    public BootcampMetricsUseCase(
            BootcampRepository bootcampRepository,
            EnrollmentRepository enrollmentRepository,
            CapabilityRepository capabilityRepository,
            TechnologyRepository technologyRepository,
            PersonRepository personRepository) {
        this.bootcampRepository = bootcampRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.capabilityRepository = capabilityRepository;
        this.technologyRepository = technologyRepository;
        this.personRepository = personRepository;
    }

    public Mono<BootcampDetailDTO> getBootcampWithMostEnrollments() {

        return bootcampRepository.findAll()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No existen bootcamps")))

                // Para cada bootcamp obtenemos el count
                .flatMap(b -> enrollmentRepository.countByBootcampId(b.getId())
                        .map(count -> Tuples.of(b, count)))

                // Ordenamos por cantidad de inscritos (descendente)
                .sort((t1, t2) -> Long.compare(t2.getT2(), t1.getT2()))
                .next() // nos quedamos con el primero (mayor count)

                .flatMap(tuple -> {
                    Bootcamp bootcamp = tuple.getT1();

                    // -------- 1. Cargar capacidades realistas ----------
                    Mono<List<Capability>> capacidadesMono = capabilityRepository
                            .findAllById(bootcamp.getCapabilityIds()).collectList();

                    // -------- 2. Cargar tecnologías por cada capacidad ----------
                    Mono<List<CapabilityDetailDTO>> capacidadesConTecnologiasMono = capacidadesMono
                            .flatMapMany(cap -> Flux.fromIterable(cap))
                            .flatMap(cap -> {

                                Mono<List<Technology>> techMono = technologyRepository
                                        .findAllById(cap.getTechnologyIds()).collectList();

                                return techMono.map(techList -> CapabilityDetailDTO.builder()
                                        .id(cap.getId())
                                        .nombre(cap.getNombre())
                                        .descripcion(cap.getDescripcion())
                                        .tecnologias(
                                                techList.stream()
                                                        .map(t -> new TechnologyDTO(t.getId(), t.getNombre(),
                                                                t.getDescripcion()))
                                                        .toList())
                                        .build());
                            })
                            .collectList();

                    // -------- 3. Cargar personas inscritas ----------
                    Mono<List<PersonDTO>> personasMono = enrollmentRepository.findByBootcampId(bootcamp.getId())
                            .flatMap(enr -> personRepository.findById(enr.getPersonId()))
                            .map(p -> new PersonDTO(p.getId(), p.getNombre(), p.getCorreo()))
                            .collectList();

                    // -------- 4. Unir todo en el DTO final ----------
                    return Mono.zip(capacidadesConTecnologiasMono, personasMono)
                            .map(data -> BootcampDetailDTO.builder()
                                    .id(bootcamp.getId())
                                    .nombre(bootcamp.getNombre())
                                    .descripcion(bootcamp.getDescripcion())
                                    .duracion(bootcamp.getDuracion())
                                    .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                                    .capacidades(data.getT1())
                                    .personasInscritas(data.getT2())
                                    .build());
                });
    }

    public Mono<BootcampDetailDTO> getMostPopularBootcamp() {

        return bootcampRepository.findAll()
                .flatMap(bootcamp -> enrollmentRepository.countByBootcampId(bootcamp.getId())
                        .map(count -> Tuples.of(bootcamp, count)))
                .sort((a, b) -> Long.compare(b.getT2(), a.getT2()))
                .next()
                .flatMap(tuple -> buildBootcampDetail(tuple.getT1()));
    }

    private Mono<BootcampDetailDTO> buildBootcampDetail(Bootcamp bootcamp) {

        Mono<List<PersonDTO>> personas = enrollmentRepository.findByBootcampId(bootcamp.getId())
                .flatMap(enr -> personRepository.findById(enr.getPersonId()))
                .map(p -> new PersonDTO(p.getId(), p.getNombre(), p.getCorreo()))
                .collectList();

        Mono<List<CapabilityDetailDTO>> capacidades = capabilityRepository.findAllById(bootcamp.getCapabilityIds())
                .flatMap(cap -> technologyRepository.findAllById(cap.getTechnologyIds())
                        .map(t -> new TechnologyDTO(t.getId(), t.getNombre(), t.getDescripcion()))
                        .collectList()
                        .map(techs -> new CapabilityDetailDTO(
                                cap.getId(),
                                cap.getNombre(),
                                cap.getDescripcion(),
                                techs)))
                .collectList();

        return Mono.zip(personas, capacidades)
                .map(tuple -> BootcampDetailDTO.builder()
                        .id(bootcamp.getId())
                        .nombre(bootcamp.getNombre())
                        .descripcion(bootcamp.getDescripcion())
                        .duracion(bootcamp.getDuracion())
                        .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                        .personasInscritas(tuple.getT1())
                        .capacidades(tuple.getT2())
                        .build());
    }
}
