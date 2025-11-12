package com.reactivo.onclass.app.on_class_reactivo.application.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.reactivo.onclass.app.on_class_reactivo.application.handler.TechnologyHandler;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TechnologyRouter {

    @Bean
    public RouterFunction<ServerResponse> technologyRoutes(TechnologyHandler handler) {
        return route(POST("/technologies"), handler::create)
                .andRoute(GET("/technologies"), handler::findAll);
    }
}