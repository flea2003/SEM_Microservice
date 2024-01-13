package nl.tudelft.sem.template.example.models;

import nl.tudelft.sem.template.example.domain.user.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSearchTest {

    @Test
    void constructorTest(){
        UserSearch u1 = new UserSearch("user", new Email("email@gmail.com"));
        assertEquals("user", u1.getUsername());
        assertEquals("email@gmail.com", u1.getEmail().getEmail());

        UserSearch u2 = new UserSearch();
        assertEquals("", u2.getUsername());
        assertNull(u2.getEmail());
    }
    @Test
    void createTest() {
        UserSearch u1 = new UserSearch("user", new Email("email@gmail.com"));
        UserSearch u2 = UserSearch.create("user","email@gmail.com");

        assertEquals(u1,u2);
    }
}