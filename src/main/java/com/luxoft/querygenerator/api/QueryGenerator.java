package com.luxoft.querygenerator.api;

public interface QueryGenerator {

    String findAll(Class<?> clazz);

    String findById(Class<?> clazz, Object id);

    String insert(Class<?> clazz, Object value);

    String remove(Class<?> clazz, Object id);

    String update(Class<?> clazz, Object value);
}
