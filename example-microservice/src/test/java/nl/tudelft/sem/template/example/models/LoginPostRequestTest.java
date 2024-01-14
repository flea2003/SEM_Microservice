package nl.tudelft.sem.template.example.models;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginPostRequestTest {

    @Test
    void getUsername() {
        LoginPostRequest loginPostRequest = new LoginPostRequest("User","pass");
        assertEquals("User", loginPostRequest.getUsername());
    }

    @Test
    void getPassword() {
        LoginPostRequest loginPostRequest = new LoginPostRequest("User","pass");
        assertEquals("pass", loginPostRequest.getPassword());
    }

    @Test
    void testEquals() {
        LoginPostRequest loginPostRequest = new LoginPostRequest("User","pass");
        LoginPostRequest loginPostRequest2 = new LoginPostRequest("User","pass2");
        LoginPostRequest loginPostRequest3 = new LoginPostRequest("User3","pass");
        LoginPostRequest loginPostRequest4 = new LoginPostRequest("User","pass");

        assertEquals(loginPostRequest,loginPostRequest);
        assertEquals(loginPostRequest,loginPostRequest4);
        assertNotEquals(loginPostRequest,loginPostRequest2);
        assertNotEquals(loginPostRequest,loginPostRequest3);
        assertNotEquals(loginPostRequest, new User());
    }

    @Test
    void testHashCode() {
        LoginPostRequest loginPostRequest = new LoginPostRequest("User","pass");
        LoginPostRequest loginPostRequest2 = new LoginPostRequest("User","pass");

        assertEquals(loginPostRequest.hashCode(),loginPostRequest2.hashCode());
        assertNotEquals(0,loginPostRequest.hashCode());
    }

    @Test
    void testToString() {
        LoginPostRequest loginPostRequest = new LoginPostRequest("User","pass");
        assertEquals("class LoginPostRequest {\n" +
                "    username: User\n" +
                "    password: pass\n" +
                "}", loginPostRequest.toString());

        LoginPostRequest loginPostRequest1 = new LoginPostRequest(null,null);
        assertEquals("class LoginPostRequest {\n" +
                "    username: null\n" +
                "    password: null\n" +
                "}", loginPostRequest1.toString());
    }
}