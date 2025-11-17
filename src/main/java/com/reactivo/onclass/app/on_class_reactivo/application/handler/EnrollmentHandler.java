package com.reactivo.onclass.app.on_class_reactivo.application.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivo.onclass.app.on_class_reactivo.application.dto.EnrollmentRequestDTO;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.EnrollmentUseCase;

import reactor.core.publisher.Mono;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class EnrollmentHandler {

    private final EnrollmentUseCase useCase;

    public EnrollmentHandler(EnrollmentUseCase useCase) {
        this.useCase = useCase;
    }

    public Mono<ServerResponse> enrollPerson(ServerRequest request) {
        return request.bodyToMono(EnrollmentRequestDTO.class)
                .flatMap(req -> useCase.enrollPerson(req.getPersonId(), req.getBootcampId()))
                .flatMap(saved -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(saved))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
    }

}
