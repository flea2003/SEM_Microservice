package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.AccountSettings.*;
import nl.tudelft.sem.template.example.domain.UserDetails.*;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.analytics.AnalyticsService;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
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
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.*;

import nl.tudelft.sem.template.example.models.UserSearch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
class UsersControllerTest {

    private static UpdateUserService updateUserService;
    private static UserRepository userRepository;
    private static UserDetailsRepository userDetailsRepository;
    private static UpdateUserDetailsService updateUserDetailsService;
    private static AccountSettingsRepository accountSettingsRepository;
    private static UserDetailsRegistrationService userDetailsRegistrationService;
    private static AccountSettingsRegistrationService accountSettingsRegistrationService;
    private static UserDetailsRegistrationService userDetailsRegistrationServiceFails;
    private static final VerificationService verificationService = new VerificationService();
    private static AnalyticsService analyticsService;
    private static UsersController sut;
    //For makeAuthor Tests
    private static DocumentConversionRequest invalidDocument1;
    private static DocumentConversionRequest invalidDocument2;
    private static DocumentConversionRequest validDocument;
    private static UserRegistrationService userRegistrationService;

    @BeforeAll
    static void setup() throws Exception {
        userRegistrationService = Mockito.mock(UserRegistrationService.class);
        updateUserService = Mockito.mock(UpdateUserService.class);
        updateUserDetailsService = Mockito.mock(UpdateUserDetailsService.class);
        userRepository = Mockito.mock(UserRepository.class);
        userDetailsRepository = Mockito.mock(UserDetailsRepository.class);
        accountSettingsRepository = Mockito.mock(AccountSettingsRepository.class);
        userDetailsRegistrationService = Mockito.mock(UserDetailsRegistrationService.class);
        userDetailsRegistrationServiceFails = Mockito.mock(UserDetailsRegistrationService.class);
        analyticsService = Mockito.mock(AnalyticsService.class);
        accountSettingsRegistrationService = Mockito.mock(AccountSettingsRegistrationService.class);

        sut = new UsersController(userRegistrationService, updateUserService, userRepository, userDetailsRepository, accountSettingsRepository, accountSettingsRegistrationService, updateUserDetailsService, userDetailsRegistrationService, analyticsService);
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
        when(userRegistrationService.registerUser("userImpossible","email@gmail.com","pass123", newDetails, newSettings)).thenThrow(new Exception("Database insertion failed"));
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
        assertEquals("Username format incorrect!", result.getBody());
    }

    @Test
    void registerInvalidEmail(){
        UserPostRequest userToAdd = new UserPostRequest("user","email","pass123");

        ResponseEntity<String> result = sut.userPost(userToAdd);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Email format incorrect!", result.getBody());
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
    void registerUserDetailsFailed() throws InvalidUserException {
        UserDetailsRegistrationService userDetailsRegistrationService1 = Mockito.mock(UserDetailsRegistrationService.class);
        when(userDetailsRegistrationService1.registerUserDetails()).thenThrow(new InvalidUserException());
        UsersController newSut = new UsersController(userRegistrationService, updateUserService, userRepository, userDetailsRepository, accountSettingsRepository, accountSettingsRegistrationService, updateUserDetailsService, userDetailsRegistrationService1, analyticsService);

        UserPostRequest userToAdd = new UserPostRequest("user","email@gmail.com","pass123");
        ResponseEntity<String> result = newSut.userPost(userToAdd);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Username or email was invalid", result.getBody());
    }

    @Test
    void registerAccountSettingsFailed() throws InvalidUserException {
        AccountSettingsRegistrationService accountSettingsRegistrationService1 = Mockito.mock(AccountSettingsRegistrationService.class);
        when(accountSettingsRegistrationService1.registerAccountSettings()).thenThrow(new InvalidUserException("Couldn't register user"));
        UsersController newSut = new UsersController(userRegistrationService, updateUserService, userRepository, userDetailsRepository, accountSettingsRepository, accountSettingsRegistrationService1, updateUserDetailsService, userDetailsRegistrationService, analyticsService);

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
        when(userRegistrationService.getUserById(1)).thenReturn(user);
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
        User added = new User("user","email@gmail.com","pass123");
        added.setId(5);
        UserDetails userDetails = new UserDetails(5, "Yoda", "Jedi", "Dagobah", "pfp", null, 10, null);
        when(userRegistrationService.getUserById(5)).thenReturn(added);
        added.setUserDetails(userDetails);
        when(userDetailsRepository.findById(5)).thenReturn(Optional.of(userDetails));
        ResponseEntity<UserDetails>response = sut.getUserDetails(5, 5);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), userDetails);
    }

    @Test
    public void getUserDetailsUserDoesntExist() {
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
        ResponseEntity<String> result = sut.editUserDetails(2, new UserDetails());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void editUserDetailsInvalidUserDetails() {
        User added = new User("user","email@gmail.com","pass123");
        added.setId(1);
        added.setIsAdmin(false);
        when(userRegistrationService.getUserById(1)).thenReturn(added);
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
        assertTrue(accountSettings.isAccountDeactivated());
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
    public void testNonExistingUser() {
        ResponseEntity<List<User>> r1 = sut.userSearchByInterests(300, new ArrayList<>());
        ResponseEntity<List<User>> r2 = sut.userSearchByBooks(300, new ArrayList<>());
        ResponseEntity<List<User>> r3 = sut.userSearchByConnections(300, new ArrayList<>());
        assertEquals(HttpStatus.NOT_FOUND, r1.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, r2.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, r3.getStatusCode());
    }

    @Test
    public void testDBError() {
        doThrow(IllegalArgumentException.class).when(userRepository).findAll();
        ResponseEntity<List<User>> r1 = sut.userSearchByInterests(1, new ArrayList<>());
        ResponseEntity<List<User>> r2 = sut.userSearchByBooks(1, new ArrayList<>());
        ResponseEntity<List<User>> r3 = sut.userSearchByConnections(1, new ArrayList<>());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r1.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r2.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r3.getStatusCode());
    }

    // Test group for the userSearchByInterest endpoint

    @Test
    public void testNullArrayOrInArray() {
        List<String> list = new ArrayList<>();
        list.add(null);
        ResponseEntity<List<User>> r1 = sut.userSearchByInterests(1, null);
        ResponseEntity<List<User>> r2 = sut.userSearchByInterests(1, list);
        assertEquals(HttpStatus.BAD_REQUEST, r1.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r2.getStatusCode());
    }

    @Test
    public void testOKInterests() {
        List<String> genres1 = List.of("fiction", "horror", "science");
        List<String> genres2 = List.of("horror");
        List<String> genres3 = List.of("cartoons");
        User u1 = new User("user100", "user100@mail.com", "pass100");
        UserDetails ud1 = new UserDetails(1000);
        ud1.setFavouriteGenres(genres1);
        u1.setId(100);
        u1.setUserDetails(ud1);

        User u2 = new User("user101", "user101@mail.com", "pass101");
        UserDetails ud2 = new UserDetails(1001);
        ud2.setFavouriteGenres(genres2);
        u2.setId(101);
        u2.setUserDetails(ud2);

        User u3 = new User("user102", "user102@mail.com", "pass102");
        UserDetails ud3 = new UserDetails(1002);
        ud3.setFavouriteGenres(genres3);
        u3.setId(102);
        u3.setUserDetails(ud3);

        doReturn(List.of(u1, u2, u3)).when(userRepository).findAll();
        ResponseEntity<List<User>> r1 = sut.userSearchByInterests(1, List.of("fiction", "horror"));
        ResponseEntity<List<User>> r2 = sut.userSearchByInterests(1, List.of("horror"));
        ResponseEntity<List<User>> r3 = sut.userSearchByInterests(1, List.of("algorithms"));
        assertEquals(r1.getBody(), List.of(u1));
        assertEquals(HttpStatus.OK, r1.getStatusCode());

        assertEquals(r2.getBody(), List.of(u1, u2));
        assertEquals(HttpStatus.OK, r2.getStatusCode());

        assertEquals(HttpStatus.NOT_FOUND, r3.getStatusCode());
    }

    // Test group for the userSearchByBooks
    @Test
    public void testNullArrayOrNullBook() {
        List<Book> books = new ArrayList<>();
        books.add(null);
        ResponseEntity<List<User>> r1 = sut.userSearchByBooks(1, null);
        ResponseEntity<List<User>> r2 = sut.userSearchByBooks(1, books);
        assertEquals(HttpStatus.BAD_REQUEST, r1.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r2.getStatusCode());
    }

    @Test
    public void testOKBooks() {
        Book b1 = new Book(1, "book1", "test", new String[0]);
        Book b2 = new Book(2, "book2", "test", new String[0]);
        Book b3 = new Book(3, "book3", "test", new String[0]);

        User u1 = new User("user100", "user100@mail.com", "pass100");
        UserDetails ud1 = new UserDetails(1000);
        ud1.setFavouriteBookID(1);
        u1.setId(100);
        u1.setUserDetails(ud1);

        User u2 = new User("user101", "user101@mail.com", "pass101");
        UserDetails ud2 = new UserDetails(1001);
        ud2.setFavouriteBookID(2);
        u2.setId(101);
        u2.setUserDetails(ud2);

        User u3 = new User("user102", "user102@mail.com", "pass102");
        UserDetails ud3 = new UserDetails(1002);
        ud3.setFavouriteBookID(1);
        u3.setId(102);
        u3.setUserDetails(ud3);
        doReturn(List.of(u1, u2, u3)).when(userRepository).findAll();

        List<Book> books1 = List.of(b1, b2);
        List<Book> books2 = List.of(b1);
        List<Book> books3 = List.of(b3);

        ResponseEntity<List<User>> r1 = sut.userSearchByBooks(1, books1);
        ResponseEntity<List<User>> r2 = sut.userSearchByBooks(1, books2);
        ResponseEntity<List<User>> r3 = sut.userSearchByBooks(1, books3);

        assertEquals(HttpStatus.OK, r1.getStatusCode());
        assertEquals(List.of(u1, u2, u3), r1.getBody());

        assertEquals(HttpStatus.OK, r2.getStatusCode());
        assertEquals(List.of(u1, u3), r2.getBody());

        assertEquals(HttpStatus.NOT_FOUND, r3.getStatusCode());
    }

    // Test group for the userSearchByConnections
    @Test
    public void testNullArrayOrNullConnections() {
        List<UserSearch> users = new ArrayList<>();
        users.add(null);
        ResponseEntity<List<User>> r1 = sut.userSearchByConnections(1, null);
        ResponseEntity<List<User>> r2 = sut.userSearchByConnections(1, users);
        assertEquals(HttpStatus.BAD_REQUEST, r1.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, r2.getStatusCode());
    }

    @Test
    public void testOKConnections() {
        User u1 = new User("user100", "user100@mail.com", "pass100");
        UserSearch us1 = new UserSearch("user100", new Email("user100@mail.com"));
        UserDetails ud1 = new UserDetails(1000);
        u1.setId(100);
        u1.setUserDetails(ud1);

        User u2 = new User("user101", "user101@mail.com", "pass101");
        UserSearch us2 = new UserSearch("user101", new Email("user101@mail.com"));
        UserDetails ud2 = new UserDetails(1001);
        u2.setId(101);
        u2.setUserDetails(ud2);

        User u3 = new User("user102", "user102@mail.com", "pass102");
        UserSearch us3 = new UserSearch("user102", new Email("user102@mail.com"));
        UserDetails ud3 = new UserDetails(1002);
        u3.setId(102);
        u3.setUserDetails(ud3);
        ud1.addFollowingItem(u3);
        ud2.addFollowingItem(u3);
        ud2.addFollowingItem(u1);
        ud3.addFollowingItem(u1);

        doReturn(List.of(u1, u2, u3)).when(userRepository).findAll();
        ResponseEntity<List<User>> r1 = sut.userSearchByConnections(1, List.of(us1, us3));
        ResponseEntity<List<User>> r2 = sut.userSearchByConnections(1, List.of(us3));
        ResponseEntity<List<User>> r3 = sut.userSearchByConnections(1, List.of(us2));
        assertEquals(List.of(u2), r1.getBody());
        assertEquals(HttpStatus.OK, r1.getStatusCode());

        assertEquals(List.of(u1, u2), r2.getBody());
        assertEquals(HttpStatus.OK, r2.getStatusCode());

        assertEquals(HttpStatus.NOT_FOUND, r3.getStatusCode());
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
    public void testUpdateNullParameter1() {
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(10000, null), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testUpdateNullParameter2() {
        AccountSettings accountSettings = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(null, accountSettings), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }


    @Test
    public void testUpdateAllOk() {
        AccountSettings accountSettings = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        User user = new User("update", "update@mail.com", "update");
        user.setAccountSettings(accountSettings);
        when(userRepository.findById(1234)).thenReturn(Optional.of(user));
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(1234, accountSettings), new ResponseEntity<>(HttpStatus.OK));
    }


    @Test
    public void hackerTriesAccountNotCorrespondingToUser() {
        AccountSettings accountSettingsSet = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        AccountSettings accountSettingsReturned = new AccountSettings(2, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        User user = new User("update", "update@mail.com", "update");
        user.setAccountSettings(accountSettingsSet);
        when(userRepository.findById(1234)).thenReturn(Optional.of(user));
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(1234, accountSettingsReturned), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void loginBadRequest(){
        assertEquals(HttpStatus.BAD_REQUEST, sut.loginUser(null).getStatusCode());

        assertEquals(HttpStatus.BAD_REQUEST, sut.loginUser(new LoginPostRequest(null,"pass")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, sut.loginUser(new LoginPostRequest("user",null)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, sut.loginUser(new LoginPostRequest("","pass")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, sut.loginUser(new LoginPostRequest("user","")).getStatusCode());
    }

    @Test
    void loginDatabaseFails(){
        when(userRegistrationService.getUserByUsername("failMe")).thenThrow(new IllegalArgumentException());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.loginUser(new LoginPostRequest("failMe","pass")).getStatusCode());
    }

    @Test
    void changePasswordExceptions(){
        when(userRepository.findById(176321)).thenThrow(new IllegalArgumentException());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.userChangePassword(176321,"newPass").getStatusCode());

        User bogus = new User();
        bogus.setId(176322);
        when(userRepository.findById(176322)).thenReturn(Optional.of(bogus));
        when(updateUserService.changePassword(eq(176322),any())).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND, sut.userChangePassword(176322,"newPass").getStatusCode());
        assertEquals("Couldn't change the password",sut.userChangePassword(176322,"newPass").getBody());
    }

    @Test
    public void getAccountSettingsNullParameter1() {
        assertEquals(sut.getAccountSettings(null, 1), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void getAccountSettingsNullParameter2() {
        assertEquals(sut.getAccountSettings(1, null), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void getAccountSettingsNoUser() {
        when(userRegistrationService.getUserById(5)).thenReturn(null);
        assertEquals(sut.getAccountSettings(5, 1), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void getAccountSettingsNoSettings() {
        when(userRegistrationService.getUserById(100)).thenReturn(new User());
        when(accountSettingsRepository.findById(2)).thenReturn(Optional.empty());
        assertEquals(sut.getAccountSettings(100, 2), new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getAccountSettingsWithNegativeID() {
        when(userRegistrationService.getUserById(100)).thenReturn(new User());
        AccountSettings badAccountSettings = new AccountSettings(-1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, true);
        when(accountSettingsRepository.findById(2)).thenReturn(Optional.of(badAccountSettings));
        assertEquals(sut.getAccountSettings(100, 2), new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void getAccountSettingsOK() {
        User user = new User();
        when(userRegistrationService.getUserById(100)).thenReturn(user);
        AccountSettings accountSettings = new AccountSettings(2, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, true);
        user.setAccountSettings(accountSettings);
        when(accountSettingsRepository.findById(2)).thenReturn(Optional.of(accountSettings));
        assertEquals(sut.getAccountSettings(100, 2), new ResponseEntity<>(accountSettings, HttpStatus.OK));
    }

    @Test
    public void getUserDetailsOrAccountSettingsNeither() {
        assertEquals(sut.getUserDetailsOrAccountSettings(1, 0), new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getUserDetailsOrAccountSettings1() {
        User user = new User();
        UserDetails userDetails = new UserDetails(10, "Name Fullname", "bio", "location",
                "", new ArrayList<>(), 1, new ArrayList<>());
        user.setUserDetails(userDetails);
        when(userRegistrationService.getUserById(1)).thenReturn(user);
        when(userDetailsRepository.findById(10)).thenReturn(Optional.of(userDetails));
        assertEquals(sut.getUserDetailsOrAccountSettings(1, 10), new ResponseEntity<>(userDetails, HttpStatus.OK));
    }

    @Test
    public void getUserDetailsOrAccountSettings2() {
        User user = new User();
        AccountSettings accountSettings = new AccountSettings(11, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, true);
        user.setAccountSettings(accountSettings);
        when(userRegistrationService.getUserById(1)).thenReturn(user);
        when(accountSettingsRepository.findById(11)).thenReturn(Optional.of(accountSettings));
        assertEquals(sut.getUserDetailsOrAccountSettings(1, 11), new ResponseEntity<>(accountSettings, HttpStatus.OK));
    }

    @Test
    public void getUserDetailsOrAccountSettingsUnauth() {
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetailsOrAccountSettings(null, 1).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetailsOrAccountSettings(1, null).getStatusCode());

        when(userRegistrationService.getUserById(191231)).thenReturn(null);
        assertEquals(HttpStatus.UNAUTHORIZED, sut.getUserDetailsOrAccountSettings(191231, 1).getStatusCode());
    }

    @Test
    void getUserDetailsBadID(){
        User user = new User("new", "new@mail.com", "new");
        UserDetails zeroID = new UserDetails();
        user.setUserDetails(zeroID);
        zeroID.setId(567210);
        when(userRegistrationService.getUserById(1)).thenReturn(user);
        when(userDetailsRepository.findById(567210)).thenReturn(Optional.of(zeroID));
        assertEquals(HttpStatus.OK, sut.getUserDetails(1, 567210).getStatusCode());

        UserDetails negativeID = new UserDetails();
        negativeID.setId(-1);
        when(userDetailsRepository.findById(567212)).thenReturn(Optional.of(negativeID));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.getUserDetails(1, 567212).getStatusCode());
    }

    @Test
    void getAccountSettingsBadID(){
        AccountSettings nonzeroID = new AccountSettings();
        nonzeroID.setId(567210);
        User user = new User();
        user.setAccountSettings(nonzeroID);
        when(userRegistrationService.getUserById(1)).thenReturn(user);
        when(accountSettingsRepository.findById(567210)).thenReturn(Optional.of(nonzeroID));
        assertEquals(HttpStatus.OK, sut.getAccountSettings(1, 567210).getStatusCode());

        AccountSettings negativeID = new AccountSettings();
        negativeID.setId(-1);
        when(accountSettingsRepository.findById(567212)).thenReturn(Optional.of(negativeID));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.getAccountSettings(1, 567212).getStatusCode());
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

        when(userRegistrationService.getUserById(80)).thenReturn(userOK);
        doReturn(detailsNew).when(updateUserDetailsService).updateUserDetails(eq(80),any());
        assertEquals(HttpStatus.OK, sut.editUserDetails(80,detailsNew).getStatusCode());
    }

    @Test
    void deleteAccountNull(){
        assertEquals(HttpStatus.UNAUTHORIZED, sut.userUserIDDelete(null).getStatusCode());
    }

    @Test
    void deactivateAccountNull(){
        assertEquals(HttpStatus.UNAUTHORIZED, sut.userUserIDDeactivatePut(null).getStatusCode());
    }

    @Test
    void searchByConnectionBadEmail(){
        UserSearch us = new UserSearch("user",null);
        assertEquals(HttpStatus.BAD_REQUEST, sut.userSearchByConnections(1,List.of(us)).getStatusCode());
    }

    @Test
    void updateAccountSettingsExceptions(){
        when(userRepository.findById(787878)).thenThrow(new NoSuchElementException());
        assertEquals(HttpStatus.NOT_FOUND, sut.userUserIDUpdateAccountSettingsPut(787878,new AccountSettings()).getStatusCode());

        AccountSettings ac = new AccountSettings();
        ac.setId(676767);
        when(accountSettingsRepository.save(ac)).thenThrow(new IllegalArgumentException());
        User toQuery = new User();
        toQuery.setId(121212);
        toQuery.setAccountSettings(ac);
        when(userRepository.findById(121212)).thenReturn(Optional.of(toQuery));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.userUserIDUpdateAccountSettingsPut(121212,ac).getStatusCode());
    }


    @Test
    void testLoginRecordedOnRegister() {
        UserPostRequest userToAdd = new UserPostRequest("user","email@gmail.com","pass123");

        ResponseEntity<String> result = sut.userPost(userToAdd);
        int userID = Integer.parseInt(Objects.requireNonNull(result.getHeaders().get("Logged in user ID")).get(0));
        User user = new User();
        user.setId(userID);

        verify(analyticsService, atLeast(1)).recordLogin(user);
    }

    @Test
    void testLoginRecordedOnLogin() {
        User user = new User("user","email@gmail.com","pass123");
        user.setId(1);
        user.setIsAdmin(false);
        when(userRegistrationService.getUserByUsername("user")).thenReturn(List.of(user));
        LoginPostRequest login = new LoginPostRequest("user", "pass123");
        ResponseEntity<User> response = sut.loginUser(login);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(analyticsService, atLeast(1)).recordLogin(user);
    }

    @Test
    void testFavoriteGenresRecordedInterests() {
        doReturn(List.of()).when(userRepository).findAll();

        User user = new User("user","email@gmail.com","pass123");
        user.setId(1);
        user.setIsAdmin(false);
        when(userRegistrationService.getUserById(1)).thenReturn(user);

        sut.userSearchByInterests(1, List.of("aTestGenre1", "aTestGenre2"));

        verify(analyticsService, times(1)).recordGenreInteraction(user, "aTestGenre1");
        verify(analyticsService, times(1)).recordGenreInteraction(user, "aTestGenre2");
    }

    @Test
    void testFavoriteGenresRecordedBooks() {
        Book b1 = new Book(1, "book1", "test", new String[0], "aTestGenre3");
        Book b2 = new Book(2, "book2", "test", new String[0], "aTestGenre4");
        Book b3 = new Book(3, "book3", "test", new String[0], "aTestGenre4");

        User user = new User("user","email@gmail.com","pass123");
        user.setId(1);
        user.setIsAdmin(false);
        when(userRegistrationService.getUserById(1)).thenReturn(user);

        sut.userSearchByBooks(1, List.of(b1, b2, b3));

        verify(analyticsService, times(1)).recordGenreInteraction(user, "aTestGenre3");
        verify(analyticsService, times(2)).recordGenreInteraction(user, "aTestGenre4");
    }
}