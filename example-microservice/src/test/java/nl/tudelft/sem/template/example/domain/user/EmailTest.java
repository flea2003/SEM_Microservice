package nl.tudelft.sem.template.example.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {
    @Test
    void testValidEmail(){
        Email e1 = new Email("a@b.cd");
        assertEquals("a@b.cd", e1.toString());
    }

    @Test
    void testInvalidEmail(){
        Email e1 = new Email("");
        assertNull(e1.toString());
        Email e2 = new Email("@");
        assertNull(e2.toString());
        Email e3 = new Email("a@b.c");
        assertNull(e3.toString());
        Email e4 = new Email("_a@b.c");
        assertNull(e4.toString());
        Email e5 = new Email("a@_b.c");
        assertNull(e5.toString());
    }
}