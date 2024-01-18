package nl.tudelft.sem.template.example.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultControllerTest {

    @Test
    void helloWorld() {
        DefaultController sut = new DefaultController();
        assertEquals("Hello", sut.helloWorld().getBody());
    }

}