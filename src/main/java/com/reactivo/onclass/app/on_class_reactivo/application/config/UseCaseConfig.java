package com.reactivo.onclass.app.on_class_reactivo.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reactivo.onclass.app.on_class_reactivo.domain.repository.BootcampRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.CapabilityRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.EnrollmentRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.repository.TechnologyRepository;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.BootcampUseCase;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.CapabilityUseCase;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.EnrollmentUseCase;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.TechnologyUseCase;

@Configuration
public class UseCaseConfig {
    @Bean
    public TechnologyUseCase technologyUseCase(TechnologyRepository repository) {
        return new TechnologyUseCase(repository);
    }

    @Bean
    public CapabilityUseCase capabilityUseCase(CapabilityRepository repository, TechnologyRepository technologyRepository) {
        return new CapabilityUseCase(repository, technologyRepository);
    }

    @Bean
    public BootcampUseCase bootcampUseCase(BootcampRepository repository, CapabilityRepository capabilityRepository, TechnologyRepository technologyRepository) {
        return new BootcampUseCase(repository, capabilityRepository, technologyRepository);
    }

    @Bean
    public EnrollmentUseCase enrollmentUseCase(EnrollmentRepository enrollmentRepository, BootcampRepository bootcampRepository) {
        return new EnrollmentUseCase(enrollmentRepository, bootcampRepository);
    }
}
