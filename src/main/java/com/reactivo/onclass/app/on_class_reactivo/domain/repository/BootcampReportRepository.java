package com.reactivo.onclass.app.on_class_reactivo.domain.repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.BootcampReport;

import reactor.core.publisher.Mono;

public interface BootcampReportRepository {

    Mono<BootcampReport> save(BootcampReport bootcamp);
}
