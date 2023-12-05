package nl.tudelft.sem.template.example.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsernameTest {

    @Test
    void testValidateAlphabetStart(){
        Username u1 = new Username("a1B1");
        assertEquals("a1B1",u1.toString());
        Username u2 = new Username("a");
        assertEquals("a",u2.toString());
        Username u3 = new Username("A9");
        assertEquals("A9",u3.toString());
    }

    @Test
    void testValidateInvalid(){
        Username u1 = new Username("1B1");
        assertNull(u1.toString());
        Username u2 = new Username("A-B1");
        assertNull(u2.toString());
        Username u3 = new Username("_");
        assertNull(u3.toString());
    }

}