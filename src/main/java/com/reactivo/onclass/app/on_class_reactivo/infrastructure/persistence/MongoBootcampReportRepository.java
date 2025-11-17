package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.BootcampReport;

public interface MongoBootcampReportRepository extends ReactiveCrudRepository<BootcampReport, String> {

}
