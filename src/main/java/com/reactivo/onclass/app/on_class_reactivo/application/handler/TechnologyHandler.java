package com.reactivo.onclass.app.on_class_reactivo.application.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Technology;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.TechnologyUseCase;

import jakarta.validation.Validator;
import reactor.core.publisher.Mono;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;

@Component
public class TechnologyHandler {

    private final TechnologyUseCase useCase;
    private final Validator validator;

    public TechnologyHandler(TechnologyUseCase useCase, Validator validator) {
        this.useCase = useCase;
        this.validator = validator;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(Technology.class)
                .flatMap(tech -> {
                    var violations = validator.validate(tech);
                    if (!violations.isEmpty()) {
                        List<String> errors = violations.stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .toList();

                        Map<String, Object> errorBody = Map.of("errors", errors);
                        return ServerResponse.badRequest()
                                .contentType(APPLICATION_JSON)
                                .bodyValue(errorBody);
                    }
                    return useCase.createTechnology(tech)
                            .flatMap(saved -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(saved))
                            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
                });
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(useCase.getAllTechnologies(), Technology.class);
    }
}
