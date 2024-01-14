package nl.tudelft.sem.template.example.models;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPostRequestTest {

    @Test
    void getUsername() {
        UserPostRequest sut = new UserPostRequest("username","email","pass");
        assertEquals("username",sut.getUsername());
    }

    @Test
    void setUsername() {
        UserPostRequest sut = new UserPostRequest("username","email","pass");
        sut.setUsername("user");
        assertEquals("user",sut.getUsername());
    }

    @Test
    void getEmail() {
        UserPostRequest sut = new UserPostRequest("username","email","pass");
        assertEquals("email",sut.getEmail());
    }

    @Test
    void setEmail() {
        UserPostRequest sut = new UserPostRequest("username","email","pass");
        sut.setEmail("e");
        assertEquals("e",sut.getEmail());
    }

    @Test
    void getPassword() {
        UserPostRequest sut = new UserPostRequest("username","email","pass");
        assertEquals("pass",sut.getPassword());
    }

    @Test
    void setPassword() {
        UserPostRequest sut = new UserPostRequest("username","email","pass");
        sut.setPassword("pass123");
        assertEquals("pass123",sut.getPassword());
    }

    @Test
    void testEquals() {
        UserPostRequest upr1 = new UserPostRequest("username","email","pass");
        UserPostRequest upr2 = new UserPostRequest("username","email","pass");
        UserPostRequest upr3 = new UserPostRequest("username","email","pass2");
        assertEquals(upr1,upr1);
        assertEquals(upr1,upr2);
        assertNotEquals(upr1,upr3);
        assertNotEquals(upr1,new User());
    }

    @Test
    void testHashCode() {
        UserPostRequest upr1 = new UserPostRequest("username","email","pass");
        UserPostRequest upr2 = new UserPostRequest("username","email","pass");
        assertEquals(upr1.hashCode(), upr2.hashCode());

        assertNotEquals(0,upr1.hashCode());
    }

    @Test
    void testToString() {
        UserPostRequest upr1 = new UserPostRequest("username","email","pass");
        assertEquals("class UserPostRequest {\n" +
                "    username: username\n" +
                "    email: email\n" +
                "    password: pass\n" +
                "}",upr1.toString());

        UserPostRequest upr2 = new UserPostRequest("username","email",null);
        assertEquals("class UserPostRequest {\n" +
                "    username: username\n" +
                "    email: email\n" +
                "    password: null\n" +
                "}",upr2.toString());
    }
}