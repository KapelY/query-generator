package com.luxoft.querygenerator.sql;

import com.luxoft.querygenerator.entity.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlQueryGeneratorTest {
    private SqlQueryGenerator sqlQueryGenerator;

    @BeforeEach
    void setUp() {
        sqlQueryGenerator = new SqlQueryGenerator();

    }

    @Test
    void findAll() {
        String expected = "SELECT id, person_name, salary FROM persons;";

        String actual = sqlQueryGenerator.findAll(Person.class);

        assertEquals(expected, actual);
    }

    @Test
    void findByIdIntOrLong() {
        String expected = "SELECT id, person_name, salary FROM persons WHERE id=1;";

        String actual = sqlQueryGenerator.findById(Person.class, 1);
        assertEquals(expected, actual);

        String actual2 = sqlQueryGenerator.findById(Person.class, 1l);
        assertEquals(expected, actual2);
    }

    @Test
    void insert() {
        String expected = "INSERT INTO persons(id, person_name, salary) VALUES (1, 'Vlad', 1.8);";

        Person person = new Person(1, "Vlad", 1.8);

        String actual = sqlQueryGenerator.insert(Person.class, person);
        assertEquals(expected, actual);
    }

    @Test
    void remove() {
        String expected = "DELETE FROM persons WHERE id=2;";

        String actual = sqlQueryGenerator.remove(Person.class, 2);
        assertEquals(expected, actual);
    }

    @Test
    void update() {
        String expected = "UPDATE persons SET salary WHERE id=3;";

        String actual = sqlQueryGenerator.update(Person.class, 2);
        assertEquals(expected, actual);
    }
}