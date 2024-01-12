package nl.tudelft.sem.template.example.domain.analytics;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
    }

}
