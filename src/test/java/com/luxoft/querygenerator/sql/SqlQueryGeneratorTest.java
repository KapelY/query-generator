package com.luxoft.querygenerator.sql;

import com.luxoft.querygenerator.entity.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlQueryGeneratorTest {
    SqlQueryGenerator sqlQueryGenerator;

    @BeforeEach
    void setUp() {
        sqlQueryGenerator = new SqlQueryGenerator(Person.class);
    }

    @Test
    void findAll() {
        String expected = "SELECT id, person_name, salary FROM persons;";

        String actual = sqlQueryGenerator.findAll();

        assertEquals(expected, actual);
    }

    @Test
    void findById() {
        SqlQueryGenerator sqlQueryGenerator = new SqlQueryGenerator(Person.class);
        String expected = "SELECT id, person_name, salary FROM persons;";

        String actual = sqlQueryGenerator.findById(2);

        assertEquals(expected, actual);
    }

    @Test
    void insert() {
    }

    @Test
    void remove() {
    }

    @Test
    void update() {
    }
}