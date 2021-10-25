package com.luxoft.querygenerator.api;

public interface QueryGenerator {

    String findAll(Class<?> clazz);

    String findById(Class<?> clazz, Object id);

    <T> String insert(Class<T> clazz, T value);

    String remove(Class<?> clazz, Object id);

    <T> String update(Class<T> clazz, T value);
}
