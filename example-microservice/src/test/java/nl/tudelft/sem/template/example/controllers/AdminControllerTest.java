package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.AccountSettings.*;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.book.BookMockApi;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.UpdateUserService;
import nl.tudelft.sem.template.example.domain.user.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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

}