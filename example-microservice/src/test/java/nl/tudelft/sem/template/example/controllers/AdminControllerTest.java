package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.accountsettings.*;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetails;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.book.BookMockApi;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.UpdateUserService;
import nl.tudelft.sem.template.example.domain.user.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AdminControllerTest {

    private static UpdateUserService updateUserService;
    private static UserRepository userRepository;
    private static UserRegistrationService userRegistrationService;
    private static UserDetailsRegistrationService userDetailsRegistrationService;
    private static BookMockApi bookMockApi;
    private static UserDetailsRepository userDetailsRepository;
    private static VerificationService verificationService;
    private static AdminController sut;

    @BeforeAll
    static void setup() throws Exception {
        userRegistrationService = Mockito.mock(UserRegistrationService.class);
        updateUserService = Mockito.mock(UpdateUserService.class);
        userRepository = Mockito.mock(UserRepository.class);
        userDetailsRepository = Mockito.mock(UserDetailsRepository.class);
        userDetailsRegistrationService = Mockito.mock(UserDetailsRegistrationService.class);
        verificationService = Mockito.mock(VerificationService.class);
        bookMockApi = Mockito.mock(BookMockApi.class);

        sut = new AdminController(userRegistrationService, updateUserService, userRepository, userDetailsRepository, userDetailsRegistrationService);

        //Invalid input registration
        UserDetails newDetails = new UserDetails(1, "Yoda", "Jedi I am",
                "Dagobah", "", null, -1, null);
        AccountSettings newSettings = new AccountSettings(7, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);

        when(userRegistrationService.registerUser("!user","email@gmail.com","pass123", newDetails, newSettings)).thenThrow(new InvalidUserException());

        User addedAdmin = new User("admin","adminemail@gmail.com","1234");
        User added = new User("user","fakeadmin@gmail.com","12345");
        addedAdmin.setId(1);
        addedAdmin.setIsAdmin(true);

        added.setId(2);
        added.setIsAdmin(false);

        when(userRegistrationService.registerUser("user","email@gmail.com","pass123", newDetails, newSettings)).thenReturn(addedAdmin);
        when(userRegistrationService.getUserById(1)).thenReturn(addedAdmin);
        when(userRegistrationService.getUserById(2)).thenReturn(added);

        //Fail to save this user
        User failedSave = new User("iFail","iFail@gmail.com", "goodPass");
        failedSave.setId(50);
        when(userRegistrationService.getUserById(50)).thenReturn(failedSave);
        when(userRepository.save(failedSave)).thenThrow(new IllegalArgumentException());
        doThrow(new IllegalArgumentException()).when(userRepository).delete(failedSave);

        //Users that do not exist
        when(userRegistrationService.getUserById(1000)).thenReturn(null);
    }
    @Test
    void adminBookPostTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        ResponseEntity<Void> result = sut.adminAdminIDAddBookPost(1, bookToAdd);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        assertFalse(sut.getBookMockApi().getBooks().isEmpty());
    }

    @Test
    void adminBookPostNotAdminTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        ResponseEntity<Void> result = sut.adminAdminIDAddBookPost(2, bookToAdd);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());

        assertTrue(sut.getBookMockApi().getBooks().isEmpty());
    }

    @Test
    void adminBookPostNullBookTest(){
        Book bookToAdd = null;

        ResponseEntity<Void> result = sut.adminAdminIDAddBookPost(1, bookToAdd);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void adminDeleteBookTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        sut.adminAdminIDAddBookPost(1, bookToAdd);
        assertFalse(sut.getBookMockApi().getBooks().isEmpty());

        ResponseEntity<Void> result = sut.adminAdminIDRemoveBookBookIDDelete(1, 1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void adminDeleteBookNotAdminTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        sut.adminAdminIDAddBookPost(1, bookToAdd);
        assertFalse(sut.getBookMockApi().getBooks().isEmpty());

        ResponseEntity<Void> result = sut.adminAdminIDRemoveBookBookIDDelete(2, 1);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertFalse(sut.getBookMockApi().getBooks().isEmpty());
    }

    @Test
    void adminUpdateBookTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        sut.adminAdminIDAddBookPost(1, bookToAdd);
        assertFalse(sut.getBookMockApi().getBooks().isEmpty());

        Book bookToUpdate = new Book(1, "Not Anymore", "", authors, "Comedy");

        ResponseEntity<Void> result = sut.adminAdminIDEditBookBookIDPut(1, 1, bookToUpdate);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(sut.getBookMockApi().getBooks().get(0).getTitle(), "Not Anymore");
    }

    @Test
    void adminUpdateBookNotAdminTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        sut.adminAdminIDAddBookPost(1, bookToAdd);
        assertFalse(sut.getBookMockApi().getBooks().isEmpty());

        Book bookToUpdate = new Book(1, "Not Anymore", "", authors, "Comedy");

        ResponseEntity<Void> result = sut.adminAdminIDEditBookBookIDPut(2, 1, bookToUpdate);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals(sut.getBookMockApi().getBooks().get(0).getTitle(), "New Book");
    }

    @Test
    void banUserNotFound() {
        //Null checks
        assertEquals(HttpStatus.NOT_FOUND, sut.banUser(1,null).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, sut.banUser(null,1).getStatusCode());

        //IDs don't exist
        assertEquals(HttpStatus.NOT_FOUND, sut.banUser(1,1000).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, sut.banUser(1000,1).getStatusCode());
    }

    @Test
    void banUserNotAdmin(){
        assertEquals(HttpStatus.UNAUTHORIZED, sut.banUser(2,1).getStatusCode());
    }

    @Test
    void banUserAdmin(){
        //No errors
        assertEquals(HttpStatus.OK, sut.banUser(1,2).getStatusCode());
        assertTrue(userRegistrationService.getUserById(2).getIsBanned());

        //Database error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.banUser(1,50).getStatusCode());
    }


    @Test
    void unbanUserNotFound() {
        //Null checks
        assertEquals(HttpStatus.NOT_FOUND, sut.unbanUser(1,null).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, sut.unbanUser(null,1).getStatusCode());

        //IDs don't exist
        assertEquals(HttpStatus.NOT_FOUND, sut.unbanUser(1,1000).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, sut.unbanUser(1000,1).getStatusCode());
    }

    @Test
    void unbanUserNotAdmin(){
        assertEquals(HttpStatus.UNAUTHORIZED, sut.unbanUser(2,1).getStatusCode());
    }

    @Test
    void unbanUserAdmin(){
        //Ban the user first
        userRegistrationService.getUserById(2).setIsBanned(true);
        //No errors
        assertEquals(HttpStatus.OK, sut.unbanUser(1,2).getStatusCode());
        assertFalse(userRegistrationService.getUserById(2).getIsBanned());

        //Database error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.unbanUser(1,50).getStatusCode());
    }
    @Test
    void deleteUserNotFound() {
        //Null checks
        assertEquals(HttpStatus.NOT_FOUND, sut.deleteUser(1,null).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, sut.deleteUser(null,1).getStatusCode());

        //IDs don't exist
        assertEquals(HttpStatus.NOT_FOUND, sut.deleteUser(1,1000).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, sut.deleteUser(1000,1).getStatusCode());
    }

    @Test
    void deleteUserNotAdmin(){
        assertEquals(HttpStatus.UNAUTHORIZED, sut.deleteUser(2,1).getStatusCode());
    }

    @Test
    void deleteUserAdmin(){
        //No errors
        assertEquals(HttpStatus.OK, sut.deleteUser(1,2).getStatusCode());
        verify(userRepository).delete(userRegistrationService.getUserById(2));

        //Database error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.deleteUser(1,50).getStatusCode());
    }
}