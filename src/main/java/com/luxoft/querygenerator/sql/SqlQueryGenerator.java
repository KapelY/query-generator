package com.luxoft.querygenerator.sql;

import com.luxoft.querygenerator.api.QueryGenerator;
import com.luxoft.querygenerator.domain.Column;
import com.luxoft.querygenerator.domain.Entity;

import java.lang.reflect.Field;
import java.util.StringJoiner;

//todo Refactor is needed, but I'm too tired it's 1:50 A.M.... some other time
public class SqlQueryGenerator implements QueryGenerator {

    @Override
    public String findAll(Class<?> clazz) {
        checkEntityAnnotationPresent(clazz);
        StringBuilder query = new StringBuilder("SELECT ");
        Entity clazzAnnotation = clazz.getAnnotation(Entity.class);
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

    @Override
    public String findById(Class<?> clazz, Object id) {
        StringBuilder query = new StringBuilder(findAll(clazz));
        query.replace(query.length() - 1, query.length(), " ");
        query.append("WHERE id=").append(id).append(";");

        // "SELECT id, person_name, salary FROM persons WHERE id=1;"
        return query.toString();
    }

    @Override
    public <T> String insert(Class<T> clazz, T value) {
        StringBuilder query = new StringBuilder("INSERT INTO ");

        Entity clazzAnnotation = clazz.getAnnotation(Entity.class);
        String tableName = clazzAnnotation.table().isEmpty() ? clazz.getName() : clazzAnnotation.table();
        query.append(tableName).append("(");
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
        query.append(columnNames).append(")");
        query.append(" VALUES (");

        T insertObject = clazz.cast(value);
        StringJoiner columnValues = new StringJoiner(", ");
        for (Field declaredField : insertObject.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            try {
                columnValues.add(String.valueOf(declaredField.get(insertObject)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        query.append(columnValues).append(")").append(";");

//      "INSERT INTO persons(id, person_name, salary) VALUES (1, Vlad, 1.8);";
        return query.toString();
    }

    @Override
    public String remove(Class<?> clazz, Object id) {
        StringBuilder query = new StringBuilder("DELETE FROM ");

        Entity clazzAnnotation = clazz.getAnnotation(Entity.class);
        String tableName = clazzAnnotation.table().isEmpty() ? clazz.getName() : clazzAnnotation.table();
        query.append(tableName);
        query.append(" WHERE id=");
        query.append(id);
        query.append(";");

//        "DELETE FROM persons WHERE id=2;"
        return query.toString();
    }

    @Override
    public <T>String update(Class<T> clazz, T value) {
        StringBuilder query = new StringBuilder("UPDATE ");
        T updateObject = clazz.cast(value);
        String resultId = "";

        Entity clazzAnnotation = clazz.getAnnotation(Entity.class);
        String tableName = clazzAnnotation.table().isEmpty() ? clazz.getName() : clazzAnnotation.table();
        query.append(tableName).append(" SET ");
        StringBuilder stringBuilder = new StringBuilder();
        final Field[] clazzDeclaredFields = clazz.getDeclaredFields();
        final Field[] valueDeclaredFields = updateObject.getClass().getDeclaredFields();
        for (int i = 0; i < clazzDeclaredFields.length; i++) {
            final Field clazzDeclaredField = clazzDeclaredFields[i];
            final Field valueDeclaredField = valueDeclaredFields[i];
            clazzDeclaredField.setAccessible(true);
            valueDeclaredField.setAccessible(true);
            Column columnAnnotation = clazzDeclaredField.getAnnotation(Column.class);
            if (clazzDeclaredField.getName().equals("id")) {
                try {
                    resultId = String.valueOf(valueDeclaredField.get(updateObject));
                    continue;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (columnAnnotation != null) {
                String columnName = columnAnnotation.name().isEmpty() ? clazzDeclaredField.getName()
                        : columnAnnotation.name();
                stringBuilder.append(columnName).append("=");
            }
            try {
                stringBuilder.append(valueDeclaredField.get(updateObject));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            stringBuilder.append(", ");
        }
        stringBuilder.replace(stringBuilder.length() - 2, stringBuilder.length(), "");

        query.append(stringBuilder).append(" WHERE id=").append(resultId).append(";");

//        "UPDATE persons SET person_name=yura, salary = 100.77 WHERE id=3;"
        return query.toString();
    }

    private void checkEntityAnnotationPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Annotation @Entity should be present");
        }
    }
}
