package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.user.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.RegistrationService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UsersControllerTest {

    private static RegistrationService registrationService;
    private static UsersController sut;
    @BeforeAll
    static void setup() throws Exception {
        registrationService = Mockito.mock(RegistrationService.class);
        sut = new UsersController(registrationService);
        //Invalid input registration
        when(registrationService.registerUser("!user","email@gmail.com","pass123")).thenThrow(new InvalidUserException());

        //Valid user -> return user object
        User added = new User("user","email@gmail.com","pass123");
        added.setId(1);
        when(registrationService.registerUser("user","email@gmail.com","pass123")).thenReturn(added);

        //Same email exists twice
        when(registrationService.getUserByEmail("iexisttwice@gmail.com")).thenReturn(added);

        //Fake a database insertion failed
        when(registrationService.registerUser("userImpossible","email@gmail.com","pass123")).thenThrow(new Exception("Database went boom"));
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
}