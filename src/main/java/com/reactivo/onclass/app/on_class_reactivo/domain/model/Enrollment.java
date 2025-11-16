package com.reactivo.onclass.app.on_class_reactivo.domain.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("enrollments")
public class Enrollment {

    @Id
    private String id;

    @NotBlank
    private String personId;

    @NotBlank
    private String bootcampId;

    private LocalDate fechaInscripcion;
}
