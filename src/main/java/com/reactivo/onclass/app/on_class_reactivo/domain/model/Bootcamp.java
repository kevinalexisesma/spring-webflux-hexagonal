package com.reactivo.onclass.app.on_class_reactivo.domain.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bootcamps")
public class Bootcamp {

    @Id
    private String id;

    @NotBlank(message = "El nombre del bootcamp es obligatorio")
    @Size(max = 80, message = "El nombre no debe superar los 80 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no debe superar los 200 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha de lanzamiento es obligatoria")
    private LocalDate fechaLanzamiento;

    @NotBlank(message = "La duración es obligatoria")
    private String duracion;

    @NotNull(message = "Debe contener al menos una capacidad")
    @Size(min = 1, max = 4, message = "Debe tener entre 1 y 4 capacidades asociadas")
    private List<Capability> capacidades;
}
