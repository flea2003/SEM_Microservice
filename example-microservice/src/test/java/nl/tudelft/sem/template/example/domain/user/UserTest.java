package nl.tudelft.sem.template.example.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testEquals() {
        //Two users are equal if they have the same id
        User u1 = new User("name1", "email1@google.com", "pass1");
        u1.setId(1);
        User u2 = new User("name1", "email1@google.com", "pass1");
        u2.setId(2);
        User u3 = new User("name2", "email2@google.com", "pass1");
        u3.setId(1);
        assertEquals(u1,u3);
        assertNotEquals(u1,u2);
    }

    @Test
    void testHashCode() {
        //Two users with same name, email, password should have the same hashcode
        User u1 = new User("name1", "email1@google.com", "pass1");
        User u2 = new User("name1", "email1@google.com", "pass1");
        assertEquals(u1.hashCode(),u2.hashCode());
    }

    @Test
    void testToString() {
        User u1 = new User("name1", "email1@google.com", "pass1");
        assertEquals("class User {\n" +
                "    id: null\n" +
                "    username: name1\n" +
                "    email: email1@google.com\n" +
                "    password: æÃÚ[ f4×óóXmt\u007Fý³k\\gWW³€Æ¥þ\\W\fqCI\n" +
                "    userDetailsID: null\n" +
                "    accountSettingsID: null\n" +
                "    isAdmin: false\n" +
                "    isAuthor: false\n" +
                "}",u1.toString());
    }
}