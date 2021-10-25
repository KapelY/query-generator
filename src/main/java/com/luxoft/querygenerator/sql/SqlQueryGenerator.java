package com.luxoft.querygenerator.sql;

import com.luxoft.querygenerator.api.QueryGenerator;
import com.luxoft.querygenerator.domain.Column;
import com.luxoft.querygenerator.domain.Entity;

import java.lang.reflect.Field;
import java.util.StringJoiner;

public class SqlQueryGenerator implements QueryGenerator {
    private Class clazz;

    public SqlQueryGenerator(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public String findAll() {
        checkEntityAnnotationPresent(clazz);
        StringBuilder query = new StringBuilder("SELECT ");
        Entity clazzAnnotation = (Entity) clazz.getAnnotation(Entity.class);
        String tableName = clazzAnnotation.table().isEmpty() ? clazz.getName() : clazzAnnotation.table();

        StringJoiner columnNames = new StringJoiner(", ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            Column columnAnnotation = declaredField.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                String columnNameFromAnnotation = columnAnnotation.name();
                String columnName = columnNameFromAnnotation.isEmpty() ? declaredField.getName()
                        : columnNameFromAnnotation;

                columnNames.add(columnName);
            }
        }

        query.append(columnNames);
        query.append(" FROM ");
        query.append(tableName);
        query.append(";");
        // SELECT id, person_name, salary from persons;
        return query.toString();
    }

    private void checkEntityAnnotationPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Annotation @Entity should be present");
        }
    }

    @Override
    public String findById(Object id) {
        findAll();
        return null;
    }

    @Override
    public String insert(Object value) {
        return null;
    }

    @Override
    public String remove(Object id) {
        return null;
    }

    @Override
    public String update(Object value) {
        return null;
    }
}
