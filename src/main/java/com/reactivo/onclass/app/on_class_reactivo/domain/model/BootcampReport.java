package com.reactivo.onclass.app.on_class_reactivo.domain.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bootcamp_reports")
public class BootcampReport {

    @Id
    private String id;

    private String bootcampId;
    private String nombreBootcamp;

    private int cantidadCapacidades;
    private int cantidadTecnologias;
    private int cantidadPersonasInscritas;

    private LocalDate fechaRegistro;
}
