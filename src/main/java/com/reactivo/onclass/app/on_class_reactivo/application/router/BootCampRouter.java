package com.reactivo.onclass.app.on_class_reactivo.application.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;

import com.reactivo.onclass.app.on_class_reactivo.application.handler.BootcampHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BootCampRouter {

    @Bean
    public RouterFunction<?> bootcampRoutes(BootcampHandler handler) {
        return route(POST("/bootcamps").and(accept(MediaType.APPLICATION_JSON)), handler::create)
                .andRoute(GET("/bootcamps"), handler::findAllPaginated);        
    }
}
