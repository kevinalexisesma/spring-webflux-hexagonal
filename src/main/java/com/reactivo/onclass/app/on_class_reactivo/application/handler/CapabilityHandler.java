package com.reactivo.onclass.app.on_class_reactivo.application.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.CapabilityUseCase;

import jakarta.validation.Validator;
import reactor.core.publisher.Mono;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class CapabilityHandler {
    private final CapabilityUseCase useCase;
    private final Validator validator;

    public CapabilityHandler(CapabilityUseCase useCase, Validator validator) {
        this.useCase = useCase;
        this.validator = validator;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(Capability.class)
                .flatMap(tech -> {
                    var violations = validator.validate(tech);
                    if (!violations.isEmpty()) {
                        return ServerResponse.badRequest().bodyValue(violations);
                    }
                    return useCase.createCapability(tech)
                            .flatMap(saved -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(saved))
                            .onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.getMessage()));
                });
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(useCase.getAllCapabilities(), Capability.class);
    }
}
