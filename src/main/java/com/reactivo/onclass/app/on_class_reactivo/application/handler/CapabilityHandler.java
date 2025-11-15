package com.reactivo.onclass.app.on_class_reactivo.application.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Capability;
import com.reactivo.onclass.app.on_class_reactivo.domain.usecase.CapabilityUseCase;

import jakarta.validation.Validator;
import reactor.core.publisher.Mono;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;

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
                        List<String> errors = violations.stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .toList();

                        Map<String, Object> errorBody = Map.of("errors", errors);
                        return ServerResponse.badRequest()
                                .contentType(APPLICATION_JSON)
                                .bodyValue(errorBody);
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

    public Mono<ServerResponse> findAllPaginated(ServerRequest request) {
        String sortBy = request.queryParam("sortBy").orElse("nombre");
        String order = request.queryParam("order").orElse("asc");
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("5"));

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(useCase.getAllCapabilities(sortBy, order, page, size), Capability.class);
    }

}
