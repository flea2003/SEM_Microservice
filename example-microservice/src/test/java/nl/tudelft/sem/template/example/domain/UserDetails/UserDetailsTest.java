package nl.tudelft.sem.template.example.domain.UserDetails;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserDetailsTest {
    @Test
    void testEquals() {
        UserDetails userDetails1 = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        UserDetails userDetails2 = new UserDetails(2, "Darth Vader", "SithLord", "Death Star", "pfp", null, 10, null);

        assertNotEquals(userDetails1, userDetails2);
        userDetails1.setId(2);
        assertEquals(userDetails1, userDetails2);
    }

    @Test
    void testConstructor() {
        UserDetails userDetails1 = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        UserDetails userDetails2 = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);

        assertEquals(userDetails1.hashCode(), userDetails2.hashCode());
    }

    @Test
    void testFollowers() {
        UserDetails userDetails1 = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        User u1 = new User("name1", "email1@google.com", "pass1");
        User u2 = new User("name1", "email1@google.com", "pass1");
        userDetails1.addFollowingItem(u1);
        userDetails1.addFollowingItem(u2);
        List<User> following = new ArrayList<>();
        following.add(u1);
        following.add(u2);
        assertEquals(userDetails1.getFollowing(), following);
    }

    @Test
    void testHash() {
        UserDetails userDetails1 = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        UserDetails userDetails2 = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        assertEquals(userDetails1.hashCode(), userDetails2.hashCode());
    }

    @Test
    void testEditUserDetails() {
        UserDetails userDetails1 = new UserDetails(1, "Yode Mester", "Jedi", "Dagobaah", "pfp", null, 11, null);
        UserDetails userDetails2 = new UserDetails(1, "Yoda Master", "Jedi", "Dagobah", "pfp", null, 10, null);
        userDetails1.editUserDetails(userDetails2);
        assertEquals(userDetails1, userDetails2);
        assertEquals(userDetails1.getId(), userDetails2.getId());
        assertEquals("Yoda Master", userDetails1.getName().getValue());
        assertEquals("Dagobah", userDetails1.getLocation());
        assertEquals(10, userDetails1.getFavouriteBookID());
    }

    @Test
    void testToString() {
        UserDetails userDetails1 = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        assertEquals(userDetails1.toString(), "class UserDetails {\n" +
                "    id: 1\n" +
                "    name: \n" +
                "    bio: Jedi\n" +
                "    location: Dagobah\n" +
                "    profilePicture: pfp\n" +
                "    following: null\n" +
                "    favouriteBookID: 10\n" +
                "    favouriteGenres: null\n" +
                "}");
    }

}
