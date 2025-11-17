package com.reactivo.onclass.app.on_class_reactivo.application.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CapabilityDetailDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private List<TechnologyDTO> tecnologias;
}