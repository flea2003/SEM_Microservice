package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.UserDetails.Name;
import nl.tudelft.sem.template.example.domain.UserDetails.UpdateUserDetailsService;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class UserDetailsControllerTest {
    private static UserRepository userRepository;
    private static UserDetailsRepository userDetailsRepository;
    private static UpdateUserDetailsService updateUserDetailsService;
    private static UserDetailsController sut;

    @BeforeAll
    public static void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userDetailsRepository = Mockito.mock(UserDetailsRepository.class);
        updateUserDetailsService = Mockito.mock(UpdateUserDetailsService.class);
        sut = new UserDetailsController(userDetailsRepository, userRepository, updateUserDetailsService);

        // User 1 - no following
        User user1 = new User("user1", "user1@mail.com", "user1");
        user1.setId(1);
        UserDetails userDetails1 = new UserDetails();
        userDetails1.setId(1);
        user1.setUserDetails(userDetails1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userDetailsRepository.save(userDetails1)).thenReturn(userDetails1);

        // User 2 - no following
        User user2 = new User("user2", "user2@mail.com", "user2");
        user2.setId(2);
        UserDetails userDetails2 = new UserDetails();
        userDetails2.setId(2);
        user2.setUserDetails(userDetails2);
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));

        // User 3 - Already follows user1
        User user3 = new User("user3", "user3@mail.com", "user3");
        user3.setId(3);
        UserDetails userDetails3 = new UserDetails();
        userDetails3.setId(3);
        userDetails3.addFollowingItem(user1);
        user3.setUserDetails(userDetails3);
        when(userRepository.findById(3)).thenReturn(Optional.of(user3));

        // User 4 - doesn't exist
        when(userRepository.findById(4)).thenReturn(Optional.empty());

        // User 5 - causes database failure
        when(userRepository.findById(5)).thenThrow(new IllegalArgumentException());

        // User 6 - null details
        User user6 = new User("user6", "user6@mail.com", "user6");
        user6.setId(6);
        user6.setUserDetails(null);
        when(userRepository.findById(6)).thenReturn(Optional.of(user6));

        // User 7 - database failure when saving details
        User user7 = new User("user7", "user7@mail.com", "user7");
        user7.setId(7);
        UserDetails userDetails7 = new UserDetails();
        userDetails7.setId(7);
        user7.setUserDetails(userDetails7);
        userDetails7.addFollowingItem(user1);
        when(userRepository.findById(7)).thenReturn(Optional.of(user7));
        when(userDetailsRepository.save(userDetails7)).thenThrow(new IllegalArgumentException());

        // User 8 - Copy of user3, used for unfollow test
        User user8 = new User("user8", "user8@mail.com", "user8");
        user8.setId(8);
        UserDetails userDetails8 = new UserDetails();
        userDetails8.setId(8);
        userDetails8.addFollowingItem(user1);
        user8.setUserDetails(userDetails8);
        when(userRepository.findById(8)).thenReturn(Optional.of(user8));

        //User 9 - copy of user 1
        User user9 = new User("user9", "user9@mail.com", "user9");
        user9.setId(9);
        UserDetails userDetails9 = new UserDetails(80, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        user9.setUserDetails(userDetails9);
        when(userRepository.findById(9)).thenReturn(Optional.of(user9));
    }

    /** Tests for followUser */
    @Test
    public void followUserSelf() {
        ResponseEntity<String> result = sut.followUser(1, 1);
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("You cannot follow yourself!", result.getBody());
    }

    @Test
    public void followUserNoUser() {
        ResponseEntity<String> result = sut.followUser(4, 1);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User could not be found", result.getBody());
    }

    @Test
    public void followUserNoFollowUser() {
        ResponseEntity<String> result = sut.followUser(1, 4);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User to follow could not be found", result.getBody());
    }

    @Test
    public void followUserDBFailure() {
        ResponseEntity<String> result1 = sut.followUser(5, 1);
        ResponseEntity<String> result2 = sut.followUser(1, 5);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result1.getStatusCode());
        assertEquals("Something went wrong", result1.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result2.getStatusCode());
        assertEquals("Something went wrong", result2.getBody());
    }

    @Test
    public void followUserNullDetails() {
        ResponseEntity<String> result = sut.followUser(6, 1);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User details could not be found", result.getBody());
    }

    @Test
    public void followUserAlreadyFollowed() {
        ResponseEntity<String> result = sut.followUser(3, 1);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("User is already being followed", result.getBody());
    }

    @Test
    public void followUserDBFailureSave() {
        ResponseEntity<String> result = sut.followUser(7, 1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Could not follow user", result.getBody());
    }

    @Test
    public void followUserOK() {
        ResponseEntity<String> result = sut.followUser(9, 2);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("You are now following user with id:2", result.getBody());

        User user1 = userRepository.findById(9).get();
        User user2 = userRepository.findById(2).get();
        assertTrue(user1.getUserDetails().isFollowed(user2));
    }

    /** Tests for unfollowUser */
    @Test
    public void unfollowUserSelf() {
        ResponseEntity<String> result = sut.unfollowUser(1, 1);
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("You cannot unfollow yourself!", result.getBody());
    }

    @Test
    public void unfollowUserNoUser() {
        ResponseEntity<String> result = sut.unfollowUser(4, 1);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User could not be found", result.getBody());
    }

    @Test
    public void unfollowUserNoFollowUser() {
        ResponseEntity<String> result = sut.unfollowUser(1, 4);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User to unfollow could not be found", result.getBody());
    }

    @Test
    public void unfollowUserDBFailure() {
        ResponseEntity<String> result1 = sut.unfollowUser(5, 1);
        ResponseEntity<String> result2 = sut.unfollowUser(1, 5);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result1.getStatusCode());
        assertEquals("Something went wrong", result1.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result2.getStatusCode());
        assertEquals("Something went wrong", result2.getBody());
    }

    @Test
    public void unfollowUserNullDetails() {
        ResponseEntity<String> result = sut.unfollowUser(6, 1);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User details could not be found", result.getBody());
    }

    @Test
    public void unfollowUserNotFollowed() {
        ResponseEntity<String> result = sut.unfollowUser(9, 2);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("User is not being followed", result.getBody());
    }

    @Test
    public void unfollowUserDBFailureSave() {
        ResponseEntity<String> result = sut.unfollowUser(7, 1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Could not follow user", result.getBody());
    }

    @Test
    public void unfollowUserOK() {
        ResponseEntity<String> result = sut.unfollowUser(8, 1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("You are no longer following user with id:1", result.getBody());

        User user8 = userRepository.findById(8).get();
        assertTrue(user8.getUserDetails().getFollowing().isEmpty());
    }

    @Test
    public void getUserDetailsParametersNull() {
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetails(null, 1).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetails(1, null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetails(null, null).getStatusCode());
    }

    @Test
    public void getUserDetailsRepositoryFail() {
        UserDetails badUserDetails = new UserDetails();
        badUserDetails.setId(-1);
        when(userDetailsRepository.findById(3)).thenReturn(Optional.of(badUserDetails));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.getUserDetails(1, 3).getStatusCode());
    }

    @Test
    public void getUserDetailsTestNoSuchUserDetails() {
        assertEquals(HttpStatus.NOT_FOUND, sut.getUserDetails(1, 2).getStatusCode());
    }

    @Test
    public void getUserDetailsAllOk() {
        User added = new User("user","email@gmail.com","pass123");
        added.setId(78);
        UserDetails userDetails = new UserDetails(79, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        when(userRepository.findById(78)).thenReturn(Optional.of(added));
        added.setUserDetails(userDetails);
        when(userDetailsRepository.findById(79)).thenReturn(Optional.of(userDetails));
        ResponseEntity<UserDetails>response = sut.getUserDetails(78, 79);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), userDetails);
    }

    @Test
    public void getUserDetailsUserDoesntExist() {
        ResponseEntity<UserDetails> response = sut.getUserDetails(4, 1);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getUserDetailsBadID(){
        User user = new User("new", "new@mail.com", "new");
        UserDetails zeroID = new UserDetails();
        user.setUserDetails(zeroID);
        zeroID.setId(567210);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userDetailsRepository.findById(567210)).thenReturn(Optional.of(zeroID));
        assertEquals(HttpStatus.OK, sut.getUserDetails(1, 567210).getStatusCode());

        UserDetails negativeID = new UserDetails();
        negativeID.setId(-1);
        when(userDetailsRepository.findById(567212)).thenReturn(Optional.of(negativeID));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.getUserDetails(1, 567212).getStatusCode());
    }

    @Test
    public void editUserDetailsBadRequest1() {
        ResponseEntity<String> result = sut.editUserDetails(null, new UserDetails());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void editUserDetailsBadRequest2() {
        ResponseEntity<String> result = sut.editUserDetails(3, null);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void editUserDetailsUserNotFound() {
        // user with id 2 does not exist
        ResponseEntity<String> result = sut.editUserDetails(4, new UserDetails());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void editUserDetailsInvalidUserDetails() {
        User added = new User("user","email@gmail.com","pass123");
        added.setId(1);
        added.setIsAdmin(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(added));
        UserDetails newDetails = new UserDetails(1, "name", "bio", "location", "profilepic", new ArrayList<>(), 5,
                new ArrayList<>());
        // this will always work, java complains because the updateUserDetails throws the exception in its signature
        try {
            when(updateUserDetailsService.updateUserDetails(1, newDetails)).thenThrow(new InvalidUserDetailsException());
        } catch (InvalidUserDetailsException e) {
            throw new RuntimeException(e);
        }
        ResponseEntity<String> result = sut.editUserDetails(1, newDetails);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("User could not be updated or new data is invalid", result.getBody());
    }

    @Test
    public void editUserDetailsUnauthorizedChanges() {
        UserDetails newDetails = new UserDetails(1, "name", "bio", "location", "profilepic", new ArrayList<>(), 5,
                new ArrayList<>());
        // this will always work, java complains because the updateUserDetails throws the exception in its signature
        try {
            when(updateUserDetailsService.updateUserDetails(any(), any())).thenThrow(new RuntimeException());
        } catch (InvalidUserDetailsException e) {
            System.out.println("no");
        }
        ResponseEntity<String> result = sut.editUserDetails(1, newDetails);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("Unauthorised changes to the user", result.getBody());
    }

    @Test
    void editUserDetailsOk() throws InvalidUserDetailsException {
        User userOK = new User();
        userOK.setId(80);
        UserDetails detailsOld = new UserDetails();
        detailsOld.setId(70);
        detailsOld.setName(new Name("Joe Biden"));
        UserDetails detailsNew = new UserDetails();
        detailsNew.setId(70);
        detailsNew.setName(new Name("Joer Bidener"));
        userOK.setUserDetails(detailsOld);

        when(userRepository.findById(80)).thenReturn(Optional.of(userOK));
        doReturn(detailsNew).when(updateUserDetailsService).updateUserDetails(eq(80),any());
        assertEquals(HttpStatus.OK, sut.editUserDetails(80,detailsNew).getStatusCode());
    }
}
