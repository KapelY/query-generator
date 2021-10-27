package com.luxoft.querygenerator.sql;

import com.luxoft.querygenerator.api.QueryGenerator;
import com.luxoft.querygenerator.domain.Column;
import com.luxoft.querygenerator.domain.Entity;
import lombok.AllArgsConstructor;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.StringJoiner;

public class SqlQueryGenerator implements QueryGenerator {
    private static final String FIELDS = ":fields";
    private static final String TABLE_NAME = ":table";
    private static final String VALUES = ":values";
    private static final String PREDICATE_ID = ":predicate_id";
    private static final String PREDICATE_VALUE = ":predicate_value";
    private static final String ID_FIELD_NAME = "id";


    @Override
    public String findAll(Class<?> clazz) {
        checkEntityAnnotationPresent(clazz);

        StringCustomizer query = new StringCustomizer("SELECT :fields FROM :table;");

        String tableName = getTableName(clazz);
        String fields = getDeclaredFields(clazz);

        query.insertValue(TABLE_NAME, tableName);
        query.insertValue(FIELDS, fields);

        // SELECT id, person_name, salary FROM persons;
        return query.query;
    }

    @Override
    public String findById(Class<?> clazz, Object id) {
        checkEntityAnnotationPresent(clazz);

        StringCustomizer query =
                new StringCustomizer("SELECT :fields FROM :table WHERE :predicate_id;");

        query.insertValue(TABLE_NAME, getTableName(clazz));
        query.insertValue(FIELDS, getDeclaredFields(clazz));
        query.insertValue(PREDICATE_ID, getPredicateId(clazz, id));

        // "SELECT id, person_name, salary FROM persons WHERE id=1;"
        return query.query;
    }

    @Override
    public String insert(Object value) throws IllegalAccessException {
        Class<?> clazz = value.getClass();
        checkEntityAnnotationPresent(clazz);

        StringCustomizer query =
                new StringCustomizer("INSERT INTO :table(:fields) VALUES(:values);");

        query.insertValue(TABLE_NAME, getTableName(clazz));
        query.insertValue(FIELDS, getDeclaredFields(clazz));
        query.insertValue(VALUES, getObjectValues(value));

//      "INSERT INTO persons(id, person_name, salary) VALUES (1, 'Vlad', 1.8);";
        return query.query;
    }

    @Override
    public String remove(Class<?> clazz, Object id) {
        checkEntityAnnotationPresent(clazz);

        StringCustomizer query =
                new StringCustomizer("DELETE FROM :table WHERE :predicate_id;");

        query.insertValue(TABLE_NAME, getTableName(clazz));
        query.insertValue(FIELDS, getDeclaredFields(clazz));
        query.insertValue(PREDICATE_ID, getPredicateId(clazz, id));

//        "DELETE FROM persons WHERE id=2;"
        return query.query;
    }

    @Override
    public String update(Class<?> clazz, Object value) throws IllegalAccessException {
        StringCustomizer query =
                new StringCustomizer("UPDATE :table SET :predicate_value WHERE :predicate_id;");

        String tableName = getTableName(clazz);
        Object id = getIdValue(value);
        String predicateId = getPredicateId(clazz, id);
        String predicateValue = getPredicateValuesExcludingId(value, id);

        query.insertValue(TABLE_NAME, tableName);
        query.insertValue(PREDICATE_VALUE, predicateValue);
        query.insertValue(PREDICATE_ID, predicateId);

//        "UPDATE persons SET person_name='yura', salary = 100.77 WHERE id=3;"
        return query.query;
    }

    private String getPredicateValuesExcludingId(Object value, Object id) throws IllegalAccessException {
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (Field declaredField : value.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);

            if (!Objects.equals(getColumnName(declaredField), ID_FIELD_NAME)) {
                var object = declaredField.get(value);
                stringJoiner.add(getColumnName(declaredField) + "=" + wrapInQuotesIfString(object));
            }
        }

        return stringJoiner.toString();
    }

    private Object getIdValue(Object value) throws IllegalAccessException {
        for (Field declaredField : value.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            var object = getColumnName(declaredField);
            if (Objects.equals(object, ID_FIELD_NAME)) {
                return declaredField.get(value);
            }
        }
        return null;
    }


    private String getObjectValues(Object value) throws IllegalAccessException {
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (Field declaredField : value.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            var object = declaredField.get(value);
            stringJoiner.add(wrapInQuotesIfString(object));
        }
        return stringJoiner.toString();
    }

    private String wrapInQuotesIfString(Object object) {
        if (object.getClass() == String.class) {
            object = "'" + object + "'";
        }
        return String.valueOf(object);
    }

    private String getDeclaredFields(Class<?> clazz) {
        StringJoiner columnNames = new StringJoiner(", ");
        for (Field declaredField : clazz.getDeclaredFields()) {
            columnNames.add(getColumnName(declaredField));
        }
        return columnNames.toString();
    }

    private String getColumnName(Field declaredField) {
        Column columnAnnotation = declaredField.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            String columnNameFromAnnotation = columnAnnotation.name();
            return columnNameFromAnnotation.isEmpty() ? declaredField.getName()
                    : columnNameFromAnnotation;
        }
        return null;
    }

    private String getTableName(Class<?> clazz) {
        Entity clazzAnnotation = clazz.getAnnotation(Entity.class);
        return clazzAnnotation.table().isEmpty() ? clazz.getName() : clazzAnnotation.table();
    }

    private String getPredicateId(Class<?> clazz, Object fieldValue) {
        StringBuilder predicate = new StringBuilder();
        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.getName().equals(SqlQueryGenerator.ID_FIELD_NAME)) {
                Column columnAnnotation = declaredField.getAnnotation(Column.class);
                if (columnAnnotation != null) {
                    String columnNameFromAnnotation = columnAnnotation.name();
                    String columnName = columnNameFromAnnotation.isEmpty() ? declaredField.getName()
                            : columnNameFromAnnotation;
                    predicate.append(columnName);
                } else {
                    predicate.append(declaredField.getName());
                }
                break;
            }
        }
        return predicate.length() == 0
                ? ""
                : predicate.append("=").append(fieldValue).toString();
    }

    private void checkEntityAnnotationPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Annotation @Entity should be present");
        }
    }

    @AllArgsConstructor
    private static final class StringCustomizer {
        private String query;

        private void insertValue(String placeholder, String value) {
            query = query.replace(placeholder, value);
        }

        @Override
        public String toString() {
            return query;
        }
    }
}
