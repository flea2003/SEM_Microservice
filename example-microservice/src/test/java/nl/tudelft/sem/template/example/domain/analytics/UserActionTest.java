package nl.tudelft.sem.template.example.domain.analytics;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserActionTest {

    @Test
    void testCreateUserAction() {
        User user = new User();
        UserAction action = new UserAction(user, "login");
        assertEquals(user, action.getUser());
        assertEquals("login", action.getType());
    }

    @Test
    void testAnalyticsEquals() {
        User user = new User();
        UserAction action0 = new UserAction(user, "login");
        action0.setId(0);
        UserAction action1 = new UserAction(user, "horror");
        action1.setId(0);
        UserAction action2 = new UserAction(user, "otherGenre");
        action2.setId(1);

        assertEquals(action0, action1);
        assertNotEquals(action0, action2);
    }

    @Test
    void testAnalyticsHashCode() {
        User user = new User();
        UserAction action0 = new UserAction(user, "login");
        action0.setId(0);
        UserAction action1 = new UserAction(user, "horror");
        action1.setId(0);

        assertEquals(action0.hashCode(), action1.hashCode());
        assertNotEquals(0,action0.hashCode());
    }

    @Test
    void equalsTests(){
        User user = new User();
        UserAction a1 = new UserAction(user,"act");
        assertTrue(a1.equals(a1));
        assertFalse(a1.equals(new User()));
    }


    @Test
    void testToString(){
        User user = new User();
        UserAction action0 = new UserAction(user, "login");

        assertEquals("User Action:\n" +
                "\tid = 0\n" +
                "\ttype = login\n" +
                "Referenced User:\n" +
                "class User {\n" +
                "    id: null\n" +
                "    username: null\n" +
                "    email: null\n" +
                "    password: null\n" +
                "    userDetails: null\n" +
                "    accountSettings: null\n" +
                "    isAdmin: null\n" +
                "    isAuthor: null\n" +
                "    isBanned: null\n" +
                "}", action0.toString());
    }

}
