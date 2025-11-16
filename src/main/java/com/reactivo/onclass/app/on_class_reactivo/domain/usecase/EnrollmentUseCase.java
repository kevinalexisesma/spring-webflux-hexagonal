package com.reactivo.onclass.app.on_class_reactivo.domain.usecase;

import java.time.LocalDate;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Bootcamp;
import com.reactivo.onclass.app.on_class_reactivo.domain.model.Enrollment;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.EnrollmentRepository;

import reactor.core.publisher.Mono;

public class EnrollmentUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final BootcampRepository bootcampRepository;

    public EnrollmentUseCase(EnrollmentRepository enrollmentRepository, BootcampRepository bootcampRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.bootcampRepository = bootcampRepository;
    }

    public Mono<Enrollment> enrollPerson(String personId, String bootcampId) {

        return enrollmentRepository.existsByPersonIdAndBootcampId(personId, bootcampId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("La persona ya está inscrita en este bootcamp"));
                    }
                    return Mono.just(true);
                })
                // 1. Validar máximo 5 bootcamps
                .then(enrollmentRepository.countByPersonId(personId))
                .flatMap(count -> {
                    if (count >= 5) {
                        return Mono.error(new IllegalArgumentException(
                                "Una persona no puede estar inscrita en más de 5 bootcamps"));
                    }
                    return Mono.just(true);
                })
                // 2. Obtener bootcamp objetivo
                .then(bootcampRepository.findById(bootcampId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Bootcamp no encontrado"))))
                // 3. Verificar conflictos por fechas
                .flatMap(bootcampObjetivo -> enrollmentRepository.findByPersonId(personId)
                        .flatMap(enr -> bootcampRepository.findById(enr.getBootcampId()))
                        .collectList()
                        .flatMap(bootcampsInscritos -> {

                            LocalDate startA = bootcampObjetivo.getFechaLanzamiento();
                            LocalDate endA = startA.plusWeeks(parseDuracion(bootcampObjetivo.getDuracion()));

                            for (Bootcamp b : bootcampsInscritos) {

                                LocalDate startB = b.getFechaLanzamiento();
                                LocalDate endB = startB.plusWeeks(parseDuracion(b.getDuracion()));

                                boolean conflict = (startA.isBefore(endB) || startA.isEqual(endB)) &&
                                        (startB.isBefore(endA) || startB.isEqual(endA));

                                if (conflict) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Conflicto de fechas con el bootcamp: " + b.getNombre()));
                                }
                            }

                            return Mono.just(bootcampObjetivo);
                        }))
                // 4. Registrar inscripción
                .flatMap(boot -> enrollmentRepository.save(
                        Enrollment.builder()
                                .bootcampId(bootcampId)
                                .personId(personId)
                                .fechaInscripcion(LocalDate.now())
                                .build()));
    }

    private int parseDuracion(String duracion) {
        try {
            return Integer.parseInt(duracion.split(" ")[0]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de duración inválido: " + duracion);
        }
    }

}
