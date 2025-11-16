package com.reactivo.onclass.app.on_class_reactivo.application.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivo.onclass.app.on_class_reactivo.application.handler.EnrollmentHandler;

@Configuration
public class EnrollmentRouter {

    @Bean
    public RouterFunction<ServerResponse> enrollmentRoutes(EnrollmentHandler handler) {
        return RouterFunctions.route()
                .POST("/enrollment", handler::enrollPerson).build();
    }
}
