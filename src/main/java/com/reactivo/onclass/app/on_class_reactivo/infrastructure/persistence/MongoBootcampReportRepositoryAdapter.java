package com.reactivo.onclass.app.on_class_reactivo.infrastructure.persistence;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.BootcampReport;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampReportRepository;

import reactor.core.publisher.Mono;

public class MongoBootcampReportRepositoryAdapter implements BootcampReportRepository {

    private final MongoBootcampReportRepository reportRepository;

    public MongoBootcampReportRepositoryAdapter(MongoBootcampReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public Mono<BootcampReport> save(BootcampReport bootcamp) {
        return reportRepository.save(bootcamp);
    }

}
