package com.reactivo.onclass.app.on_class_reactivo.domain.repository;

import com.reactivo.onclass.app.on_class_reactivo.domain.model.Person;

import reactor.core.publisher.Mono;

public interface PersonRepository {

    Mono<Person> findById(String id);
}
