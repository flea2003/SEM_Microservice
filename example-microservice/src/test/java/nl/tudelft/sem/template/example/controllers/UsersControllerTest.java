package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.models.DocumentConversionRequest;
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

    private static RegistrationService registrationService;
    private static UserRepository userRepository;
    private static VerificationService verificationService;
    private static UsersController sut;

    //For makeAuthor Tests
    private static DocumentConversionRequest invalidDocument1;
    private static DocumentConversionRequest invalidDocument2;
    private static DocumentConversionRequest validDocument;
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
        User testAdmin = new User("admin", "admin@mail.com", "adminpass");
        testAdmin.setId(2);
        testAdmin.setIsAdmin(true);
        when(userRepository.findById(2)).thenReturn(Optional.of(testAdmin));

        //Database failure
        when(userRepository.findById(500)).thenThrow(new IllegalStateException("Database failure"));

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

        ResponseEntity<String> result = sut.makeAuthor(1000, validDocument);
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
}