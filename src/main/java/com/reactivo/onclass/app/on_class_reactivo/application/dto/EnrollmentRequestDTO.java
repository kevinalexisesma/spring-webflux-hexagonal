package com.reactivo.onclass.app.on_class_reactivo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentRequestDTO {
    private String personId;
    private String bootcampId;
}
