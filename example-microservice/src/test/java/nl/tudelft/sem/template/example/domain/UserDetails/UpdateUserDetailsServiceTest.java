package nl.tudelft.sem.template.example.domain.UserDetails;

import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UpdateUserDetailsServiceTest {

    private static UserRepository userRepository;
    private static UserDetailsRepository userDetailsRepository;
    private static UpdateUserDetailsService sut;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userDetailsRepository = Mockito.mock(UserDetailsRepository.class);
        sut = new UpdateUserDetailsService(userRepository, userDetailsRepository);
    }

    @Test
    void arrayHasNullsUtilityTrue(){
        List<String> test = Arrays.asList("", "abc", null, "a");
        assertTrue(sut.arrayHasNullsUtility(test));
    }

    @Test
    void arrayHasNullsUtilityFalse(){
        List<String> test = Arrays.asList("", "abc", "", "a");
        assertFalse(sut.arrayHasNullsUtility(test));
    }

    @Test
    void checkValidUserDetailsFalse() {
        UserDetails ud = new UserDetails(null, "name", "bio", "location",
                "profilepic", new ArrayList<>(), 5, new ArrayList<>());
        assertFalse(sut.checkValidUserDetails(ud));
    }

    @Test
    void checkValidUserDetailsTrue() {
        assertTrue(sut.checkValidUserDetails(new UserDetails(1, "name", "bio", "location",
                "profilepic", new ArrayList<>(), 5, new ArrayList<>())));
    }

    @Test
    void updateUserDetailsNoUser() throws InvalidUserDetailsException {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertNull(sut.updateUserDetails(1, new UserDetails()));
    }

    @Test
    void updateUserDetailsBadData() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        UserDetails newDetails = new UserDetails(null, "Name Fullname", "bio", "location",
                "profilepic", new ArrayList<>(), 5, new ArrayList<>());
        assertThrows(InvalidUserDetailsException.class, () -> sut.updateUserDetails(1, newDetails),
                "New user details data is invalid");
    }

    @Test
    void updateUserDetailsOK() throws InvalidUserDetailsException {
        UserDetails newDetails = new UserDetails(1, "Name Fullname", "bio", "location",
                "profilepic", new ArrayList<>(), 5, new ArrayList<>());
        User user = new User("username", "email", "password", new UserDetails(), new AccountSettings());
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userDetailsRepository.save(any())).thenReturn(newDetails);
        assertEquals(newDetails, sut.updateUserDetails(1, newDetails));
        assertEquals("Name Fullname", user.getUserDetails().getName().getName());
    }
}