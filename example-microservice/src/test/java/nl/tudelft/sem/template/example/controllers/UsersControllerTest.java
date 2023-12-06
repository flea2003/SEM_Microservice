package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

class UsersControllerTest {

    private static RegistrationService registrationService;
    private static UserRepository userRepository;
    private static VerificationService verificationService = new VerificationService();
    private static UsersController sut;
    @BeforeAll
    static void setup() throws Exception {
        registrationService = Mockito.mock(RegistrationService.class);
        userRepository = Mockito.mock(UserRepository.class);
        sut = new UsersController(registrationService, userRepository);
        //Invalid input registration
        when(registrationService.registerUser("!user","email@gmail.com","pass123")).thenThrow(new InvalidUserException());

        //Valid user -> return user object
        User added = new User("user","email@gmail.com","pass123");
        added.setId(1);
        added.setIsAdmin(false);
        when(registrationService.registerUser("user","email@gmail.com","pass123")).thenReturn(added);

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
    public void makeAdminInvalidPassword() {
        ResponseEntity<String> result = sut.makeAdmin(1, "GuessingAdminPassword");
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("Invalid password!", result.getBody());
    }

    @Test
    public void makeAdminAlreadyAdmin() {
        ResponseEntity<String> result = sut.makeAdmin(2, "bookManiaAdminPassword@Admin");
        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals("You are already an admin!", result.getBody());
    }

    @Test
    public void makeAdminNormalUser() {
        ResponseEntity<String> result = sut.makeAdmin(1, "bookManiaAdminPassword@Admin");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User with ID:1 is now an admin", result.getBody());
    }
}