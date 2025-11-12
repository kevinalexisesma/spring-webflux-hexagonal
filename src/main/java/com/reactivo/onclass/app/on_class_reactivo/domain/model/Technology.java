package com.reactivo.onclass.app.on_class_reactivo.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "technologies")
public class Technology {

    @Id
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 90, message = "La descripción no puede superar los 90 caracteres")
    private String descripcion;
}