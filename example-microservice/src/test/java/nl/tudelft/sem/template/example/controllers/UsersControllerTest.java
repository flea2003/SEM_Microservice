package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.domain.user.RegistrationService;
import nl.tudelft.sem.template.example.domain.user.UpdateUserService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

class UsersControllerTest {

    private static UpdateUserService updateUserService;
    private static UserRepository userRepository;
    private static final VerificationService verificationService = new VerificationService();
    private static UsersController sut;
    @BeforeAll
    static void setup() throws Exception {
        RegistrationService registrationService = Mockito.mock(RegistrationService.class);
        updateUserService = Mockito.mock(UpdateUserService.class);
        userRepository = Mockito.mock(UserRepository.class);
        sut = new UsersController(registrationService, updateUserService ,userRepository);
        //Invalid input registration
        when(registrationService.registerUser("!user","email@gmail.com","pass123")).thenThrow(new InvalidUserException());

        //Valid user -> return user object
        User added = new User("user","email@gmail.com","pass123");
        added.setId(1);
        added.setIsAdmin(false);
        when(registrationService.registerUser("user","email@gmail.com","pass123")).thenReturn(added);
        when(registrationService.getUserById(1)).thenReturn(added);

        //Same email exists twice
        when(registrationService.getUserByEmail("iexisttwice@gmail.com")).thenReturn(added);

        //Fake a database insertion failed
        when(registrationService.registerUser("userImpossible","email@gmail.com","pass123")).thenThrow(new Exception("Database went boom"));

        //Mock an existing user in the database
        when(userRepository.findById(1)).thenReturn(Optional.of(added));

        //Valid user that is an admin
        User testAdmin = new User("admin", "admin@gmail.com", "adminpass");
        testAdmin.setId(2);
        testAdmin.setIsAdmin(true);
        when(userRepository.findById(2)).thenReturn(Optional.of(testAdmin));

        //Database failure
        when(userRepository.findById(500)).thenThrow(new IllegalStateException("Database failure"));
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
}