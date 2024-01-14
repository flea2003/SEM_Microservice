package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
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
        assertEquals(u1,u1);
        assertEquals(u1,u3);
        assertNotEquals(u1,u2);
        assertNotEquals(u1,new UserDetails());
    }

    @Test
    void testHashCode() {
        //Two users with same name, email, password should have the same hashcode
        User u1 = new User("name1", "email1@google.com", "pass1");
        User u2 = new User("name1", "email1@google.com", "pass1");
        assertEquals(u1.hashCode(),u2.hashCode());
        assertNotEquals(0,u1.hashCode());
    }

    @Test
    void testToString(){
        User u1 = new User();
        u1.setId(5);
        u1.setEmail(new Email("email@gmail.com"));

        assertEquals("class User {\n" +
                "    id: 5\n" +
                "    username: null\n" +
                "    email: email@gmail.com\n" +
                "    password: null\n" +
                "    userDetails: null\n" +
                "    accountSettings: null\n" +
                "    isAdmin: null\n" +
                "    isAuthor: null\n" +
                "    isBanned: null\n" +
                "}", u1.toString());
    }
}