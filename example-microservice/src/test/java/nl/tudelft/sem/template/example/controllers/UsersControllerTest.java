package nl.tudelft.sem.template.example.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.example.domain.AccountSettings.*;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.analytics.AnalyticsService;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.models.DocumentConversionRequest;
import nl.tudelft.sem.template.example.domain.user.UpdateUserService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.models.LoginPostRequest;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class UsersControllerTest {

    private static UpdateUserService updateUserService;
    private static UserRepository userRepository;
    private static UserDetailsRepository userDetailsRepository;
    private static AccountSettingsRepository accountSettingsRepository;
    private static UserDetailsRegistrationService userDetailsRegistrationService;
    private static AccountSettingsRegistrationService accountSettingsRegistrationService;
    private static UserDetailsRegistrationService userDetailsRegistrationServiceFails;
    private static final VerificationService verificationService = new VerificationService();
    private static AnalyticsService analyticsService;
    private static UsersController sut;
    private static AccountSettingsRepository accountSettingsRepository;
    private static AccountSettingsRegistrationService accountSettingsRegistrationService;
    //For makeAuthor Tests
    private static DocumentConversionRequest invalidDocument1;
    private static DocumentConversionRequest invalidDocument2;
    private static DocumentConversionRequest validDocument;
    private static UserRegistrationService userRegistrationService;

    @BeforeAll
    static void setup() throws Exception {
        userRegistrationService = Mockito.mock(UserRegistrationService.class);
        updateUserService = Mockito.mock(UpdateUserService.class);
        userRepository = Mockito.mock(UserRepository.class);
        userDetailsRepository = Mockito.mock(UserDetailsRepository.class);
        accountSettingsRepository = Mockito.mock(AccountSettingsRepository.class);
        userDetailsRegistrationService = Mockito.mock(UserDetailsRegistrationService.class);
        userDetailsRegistrationServiceFails = Mockito.mock(UserDetailsRegistrationService.class);
        analyticsService = Mockito.mock(AnalyticsService.class);
        accountSettingsRegistrationService = Mockito.mock(AccountSettingsRegistrationService.class);

        sut = new UsersController(userRegistrationService, updateUserService, userRepository, userDetailsRepository, accountSettingsRepository, userDetailsRegistrationService, accountSettingsRegistrationService, analyticsService);
        //Invalid input registration
        UserDetails newDetails = new UserDetails(1, "Yoda", "Jedi I am",
                "Dagobah", "", null, -1, null);
        AccountSettings newSettings = new AccountSettings(7, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        when(accountSettingsRegistrationService.registerAccountSettings()).thenReturn(newSettings);
        when(userRegistrationService.registerUser("!user","email@gmail.com","pass123", newDetails, newSettings)).thenThrow(new InvalidUserException());

        //Valid user -> return user object
        User added = new User("user","email@gmail.com","pass123");
        added.setId(1);
        added.setIsAdmin(false);
        when(userRegistrationService.registerUser("user","email@gmail.com","pass123", newDetails, newSettings)).thenReturn(added);
        when(userRegistrationService.getUserById(1)).thenReturn(added);
        when(userRegistrationService.getUserById(2)).thenReturn(null);
        when(userDetailsRegistrationService.registerUserDetails()).thenReturn( new UserDetails(1, "Yoda", "Jedi I am",
                "Dagobah", "", null, -1, null));
        when(userDetailsRegistrationServiceFails.registerUserDetails()).thenThrow(new InvalidUserException());

        //Same email exists twice
        when(userRegistrationService.getUserByEmail("iexisttwice@gmail.com")).thenReturn(added);

        //Fake a database insertion failed
        when(userRegistrationService.registerUser("userImpossible","email@gmail.com","pass123", newDetails, newSettings)).thenThrow(new Exception("Database went boom"));
        //Mock an existing user in the database
        when(userRepository.findById(1)).thenReturn(Optional.of(added));

        //Valid user that is an admin
        User testAdmin = new User("admin", "admin@mail.com", "adminpass");
        testAdmin.setId(2);
        testAdmin.setIsAdmin(true);
        when(userRepository.findById(2)).thenReturn(Optional.of(testAdmin));

        //Database failure
        when(userRepository.findById(500)).thenThrow(new IllegalStateException("Database failure"));

        // We have a userDetail which is valid
        UserDetails userDetails = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);

        when(userDetailsRepository.findById(1)).thenReturn(Optional.of(userDetails));
        // Here some userDetail which is not valid
        when(userDetailsRepository.findById(2)).thenReturn(Optional.empty());
        when(userDetailsRepository.findById(3)).thenThrow(new IllegalArgumentException("Boom!"));


        //For makeAuthor tests
        invalidDocument1 = new DocumentConversionRequest(100);
        invalidDocument2 = new DocumentConversionRequest(72501234);
        validDocument = new DocumentConversionRequest(10501234);

        //Valid user that is an author
        User testAuthor = new User("author", "author@mail.com", "authorpass");
        testAuthor.setId(3);
        testAuthor.setIsAuthor(true);
        when(userRepository.findById(3)).thenReturn(Optional.of(testAuthor));
    }
    @Test
    void registerEmptyInput(){
        UserPostRequest userToAdd = new UserPostRequest("user","email@gmail.com","");

        ResponseEntity<String> result = sut.userPost(userToAdd);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Request body is malformed", result.getBody());
    }

    @Test
    void registerEmailAlreadyExistsInput(){
        UserPostRequest userToAddSameEmail = new UserPostRequest("user2","iexisttwice@gmail.com","pass");

        ResponseEntity<String> result = sut.userPost(userToAddSameEmail);
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("User with email already exists", result.getBody());
    }

    @Test
    void registerInvalidUsername(){
        UserPostRequest userToAdd = new UserPostRequest("!user","email@gmail.com","pass123");

        ResponseEntity<String> result = sut.userPost(userToAdd);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Username or email format was incorrect", result.getBody());
    }

    @Test
    void registerDatabaseFailed(){
        UserPostRequest userToAdd = new UserPostRequest("userImpossible","email@gmail.com","pass123");

        ResponseEntity<String> result = sut.userPost(userToAdd);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Database insertion failed", result.getBody());
    }

    @Test
    void registerOk(){
        UserPostRequest userToAdd = new UserPostRequest("user","email@gmail.com","pass123");

        ResponseEntity<String> result = sut.userPost(userToAdd);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User created successfully", result.getBody());
        assertEquals("1", Objects.requireNonNull(result.getHeaders().get("Logged in user ID")).get(0));
    }

    @Test
    void registerUserDetailsFailed(){
        UsersController newSut = new UsersController(userRegistrationService,updateUserService,userRepository,userDetailsRepository, accountSettingsRepository, userDetailsRegistrationServiceFails, accountSettingsRegistrationService, analyticsService);

        UserPostRequest userToAdd = new UserPostRequest("user","email@gmail.com","pass123");

        ResponseEntity<String> result = newSut.userPost(userToAdd);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Couldn't register user", result.getBody());
    }

    @Test
    public void makeAdminNonExistingUser() {
        ResponseEntity<String> result = sut.makeAdmin(300, "bookManiaAdminPassword@Admin");
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Username with that ID could not be found", result.getBody());
    }

    @Test
    public void makeAdminDBFailure() {
        ResponseEntity<String> result = sut.makeAdmin(500, "bookManiaAdminPassword@Admin");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Something went wrong", result.getBody());
    }

    @Test
    public void makeAdminInvalidPassword() {
        ResponseEntity<String> result = sut.makeAdmin(1, "GuessingAdminPassword");
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("Invalid password!", result.getBody());
    }

    @Test
    public void makeAdminNullPassword() {
        ResponseEntity<String> result = sut.makeAdmin(1, null);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("Password cannot be null", result.getBody());
    }

    @Test
    public void makeAdminAlreadyAdmin() {
        ResponseEntity<String> result = sut.makeAdmin(2, "bookManiaAdminPassword@Admin");
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("You are already an admin!", result.getBody());
    }

    @Test
    public void makeAdminUnableToSave() {
        User toMake = new User("fail", "fail@mail.com", "failpass");
        toMake.setId(1000);
        when(userRepository.findById(1000)).thenReturn(Optional.of(toMake));
        when(userRepository.save(toMake)).thenThrow(new IllegalStateException("Database failure"));

        ResponseEntity<String> result = sut.makeAdmin(1000, "bookManiaAdminPassword@Admin");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("User could not be updated", result.getBody());
    }

    @Test
    public void makeAdminNormalUser() {
        User toMake = new User("success", "success@mail.com", "successpass");
        toMake.setId(1000);
        when(userRepository.findById(1000)).thenReturn(Optional.of(toMake));
        when(userRepository.save(toMake)).thenReturn(toMake);

        ResponseEntity<String> result = sut.makeAdmin(1000, "bookManiaAdminPassword@Admin");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User with ID:1000 is now an admin", result.getBody());
        assertTrue(toMake.getIsAdmin());
    }

    @Test
    public void changePasswordOK() {
        User toChange = new User("Jim", "jim@mail.com", "oldpassword");
        toChange.setId(3);
        User changed = new User("Jim", "jim@mail.com", "newpassword");
        changed.setId(3);
        when(userRepository.findById(3)).thenReturn(Optional.of(toChange));
        HashedPassword hashedPass = PasswordHashingService.hash("newpassword");
        when(updateUserService.changePassword(3, hashedPass)).thenReturn(changed);

        ResponseEntity<String> result = sut.userChangePassword(3, "newpassword");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Your password has been changed successfully.", result.getBody());
        assertEquals(toChange.getPassword(), PasswordHashingService.hash("newpassword"));
    }

    @Test
    public void changePasswordWrongId() {
        ResponseEntity<String> result = sut.userChangePassword(20, "newpassword");
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User with that ID could not be found", result.getBody());
    }

    @Test
    public void changePasswordNullUserId() {
        ResponseEntity<String> result = sut.userChangePassword(null, "newpassword");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Request body is malformed", result.getBody());
    }

    @Test
    public void changePasswordEmptyBody() {
        ResponseEntity<String> result = sut.userChangePassword(null, "");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Request body is malformed", result.getBody());
    }

    @Test
    public void changePasswordDatabaseFailure() {
        User toChange = new User("Kevin", "kevin@mail.com", "oldpassword");
        toChange.setId(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(toChange));
        when(updateUserService.changePassword(5, PasswordHashingService.hash("newpassword")))
                .thenThrow(new IllegalStateException("Database failure"));

        ResponseEntity<String> result = sut.userChangePassword(5, "newpassword");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Database insertion failed", result.getBody());
    }

    @Test
    public void getUserTestOk() {
        User user = new User("user","email@gmail.com","pass123");
        user.setId(1);
        user.setIsAdmin(false);

        ResponseEntity<User> result = sut.userGetUser(1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(user, result.getBody());
    }

    @Test
    public void getUserTestNotFound() {
        ResponseEntity<User> result = sut.userGetUser(12);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void getUserTestInternalError() {
        User user = new User("fake_user","iamfake@gmail.com","password");
        user.setId(999);
        user.setIsAdmin(false);
        when(sut.userGetUser(999)).thenThrow(new IllegalStateException("Database failure"));

        ResponseEntity<User> result = sut.userGetUser(999);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }


    @Test
    public void getUserDetailsParametersNull() {
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetails(null, 1).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetails(1, null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetails(null, null).getStatusCode());
    }

    @Test
    public void getUserDetailsRepositoryFail() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.getUserDetails(1, 3).getStatusCode());
    }

    @Test
    public void getUserDetailsTestNoSuchUserDetails() {
        assertEquals(HttpStatus.NOT_FOUND, sut.getUserDetails(1, 2).getStatusCode());
    }

    @Test
    public void getUserDetailsAllOk() {
        UserDetails userDetails = new UserDetails(1, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        ResponseEntity<UserDetails>response = sut.getUserDetails(1, 1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), userDetails);
    }

    @Test
    public void getUserDetailsUserDesontExist() {
        ResponseEntity<UserDetails>response = sut.getUserDetails(2, 1);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void makeAuthorBadRequest() {
        ResponseEntity<String> r1 = sut.makeAuthor(1, null);
        ResponseEntity<String> r2 = sut.makeAuthor(1, new DocumentConversionRequest(null));
        assertEquals(HttpStatus.BAD_REQUEST, r1.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r2.getStatusCode());

        assertEquals("Request body is malformed", r1.getBody());
        assertEquals("Request body is malformed", r2.getBody());
    }

    @Test
    public void makeAuthorNoUser() {
        ResponseEntity<String> result = sut.makeAuthor(300, validDocument);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User with that ID could not be found", result.getBody());
    }

    @Test
    public void makeAuthorDBFailure() {
        ResponseEntity<String> result = sut.makeAuthor(500, validDocument);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Something went wrong", result.getBody());
    }

    @Test
    public void makeAuthorAlreadyAuthor() {
        ResponseEntity<String> result = sut.makeAuthor(3, validDocument);
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("You are already an author!", result.getBody());
    }

    @Test
    public void makeAuthorInvalidDocument() {
        ResponseEntity<String> r1 = sut.makeAuthor(1, invalidDocument1);
        ResponseEntity<String> r2 = sut.makeAuthor(1, invalidDocument2);

        assertEquals(HttpStatus.UNAUTHORIZED, r1.getStatusCode());
        assertEquals("Document not valid", r1.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, r2.getStatusCode());
        assertEquals("Document not valid", r2.getBody());
    }

    @Test
    public void makeAuthorUnableToSave() {
        User toMake = new User("fail", "fail@mail.com", "failpass");
        toMake.setId(10000);
        when(userRepository.findById(10000)).thenReturn(Optional.of(toMake));
        when(userRepository.save(toMake)).thenThrow(new IllegalStateException("DB failure"));

        ResponseEntity<String> result = sut.makeAuthor(10000, validDocument);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("User could not be saved", result.getBody());
    }

    @Test
    public void makeAuthorOk() {
        User toMake = new User("author", "author@mail.com", "authorpass");
        toMake.setId(1000);
        when(userRepository.findById(1000)).thenReturn(Optional.of(toMake));
        when(userRepository.save(toMake)).thenReturn(toMake);
        ResponseEntity<String> result = sut.makeAuthor(1000, validDocument);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User with ID:1000 is now an author", result.getBody());
        assertTrue(toMake.getIsAuthor());
    }

    @Test
    public void userUserIDDeleteGood() {
        User toDelete = new User("delete", "delete@mail.com", "delete");
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDelete));
        assertEquals(sut.userUserIDDelete(10000), new ResponseEntity<>(HttpStatus.OK));
    }

    @Test
    public void userUserIDDeleteUserDoesntExist() {
        when(userRepository.findById(10000)).thenReturn(Optional.empty());
        assertEquals(sut.userUserIDDelete(10000), new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Test
    public void userUserIDDeleteUserCouldntDelete() {
        User toDelete = new User("delete", "delete@mail.com", "delete");
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDelete));
        doThrow(new IllegalArgumentException()).when(userRepository).delete(toDelete);
        assertEquals(sut.userUserIDDelete(10000), new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void userUserIDDeactivateGood() {
        User toDeactivate = new User("delete", "delete@mail.com", "delete");
        AccountSettings accountSettings = new AccountSettings(420, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        toDeactivate.setAccountSettings(accountSettings);
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDeactivate));
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.OK));
        assertEquals(accountSettings.isAccountDeactivated(), true);
    }

    @Test
    public void userUserIDDeactivateDoesntExist() {
        when(userRepository.findById(10000)).thenReturn(Optional.empty());
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Test
    public void userUserIDDeactivateCouldntDeactivate() {
        User toDeactivate = new User("delete", "delete@mail.com", "delete");
        AccountSettings accountSettings = new AccountSettings(420, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        toDeactivate.setAccountSettings(accountSettings);
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDeactivate));
        doThrow(new IllegalArgumentException()).when(accountSettingsRepository).save(accountSettings);
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void userUserIDDeactivateNoAccountSettings() {
        User toDeactivate = new User("delete", "delete@mail.com", "delete");
        toDeactivate.setAccountSettings(null);
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDeactivate));
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }


    @Test
    void userSearchTestOk() {
        String query = "user";

        // Sample list of users matching the search query
        List<User> matchingUsers = new ArrayList<>();
        matchingUsers.add(new User("user1", "user1@example.com", "pass123"));
        matchingUsers.add(new User("user2", "user2@example.com", "pass456"));

        when(userRegistrationService.getUserByUsername(query)).thenReturn(matchingUsers);

        ResponseEntity<List<User>> result = sut.userSearch(1, query);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(matchingUsers, result.getBody());
    }

    @Test
    void userSearchTestNotFound() {
        String query = "user10000";

        ResponseEntity<List<User>> result = sut.userSearch(1, query);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void userSearchNullName() {
        String query = null;

        ResponseEntity<List<User>> result = sut.userSearch(1, query);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void userSearchEmptyName() {
        String query = "";

        ResponseEntity<List<User>> result = sut.userSearch(1, query);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void userSearchWithNonExistingUser() {
        String query = "user";

        // Sample list of users matching the search query
        List<User> matchingUsers = new ArrayList<>();
        matchingUsers.add(new User("user1", "user1@example.com", "pass123"));
        matchingUsers.add(new User("user2", "user2@example.com", "pass456"));

        when(userRegistrationService.getUserByUsername(query)).thenReturn(matchingUsers);

        ResponseEntity<List<User>> result = sut.userSearch(2000, query);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }
    @Test
    public void testUpdateNullParameter1() throws Exception{
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(10000, null), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testUpdateNullParameter2() throws Exception{
        AccountSettings accountSettings = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(null, accountSettings), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }


    @Test
    public void testUpdateAllOk() throws Exception{
        AccountSettings accountSettings = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        User user = new User("update", "update@mail.com", "update");
        user.setAccountSettings(accountSettings);
        when(userRepository.findById(1234)).thenReturn(Optional.of(user));
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(1234, accountSettings), new ResponseEntity<>(HttpStatus.OK));
    }


    @Test
    public void hackerTriesAccountNotCorrespondingToUser() throws Exception{
        AccountSettings accountSettingsSet = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        AccountSettings accountSettingsReturned = new AccountSettings(2, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        User user = new User("update", "update@mail.com", "update");
        user.setAccountSettings(accountSettingsSet);
        when(userRepository.findById(1234)).thenReturn(Optional.of(user));
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(1234, accountSettingsReturned), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }


}