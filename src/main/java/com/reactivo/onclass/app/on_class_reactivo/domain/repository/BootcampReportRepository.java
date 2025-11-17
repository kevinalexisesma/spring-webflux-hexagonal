package com.reactivo.onclass.app.on_class_reactivo.domain.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.BootcampReport;

public interface BootcampReportRepository extends ReactiveCrudRepository<BootcampReport, String> {
}
