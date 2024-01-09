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
import nl.tudelft.sem.template.example.models.DocumentConversionRequest;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.UpdateUserService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.Optional;

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

        //Valid user -> return user object
        User added = new User("admin","adminemail@gmail.com","1234");
        added.setId(1);
        added.setIsAdmin(true);
        when(userRegistrationService.registerUser("user","email@gmail.com","pass123", newDetails, newSettings)).thenReturn(added);

        when()
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

}