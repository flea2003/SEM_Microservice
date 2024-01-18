package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.validation.Valid;
import lombok.Getter;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.book.BookMockApi;
import nl.tudelft.sem.template.example.domain.user.UpdateUserService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.domain.user.VerificationService;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.strategy.AdminAuthentication;
import nl.tudelft.sem.template.example.strategy.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals"})
public class AdminController {
    transient UserRegistrationService userRegistrationService;
    transient UserDetailsRegistrationService userDetailsRegistrationService;
    @Getter
    BookMockApi bookMockApi;
    transient UserRepository userRepository;
    transient UserDetailsRepository userDetailsRepository;
    transient VerificationService verificationService;
    transient UpdateUserService updateUserService;

    /**
     * Constructor for the admin controller.
     */
    @Autowired
    public AdminController(UserRegistrationService userRegistrationService, UpdateUserService updateUserService,
                           UserRepository userRepository, UserDetailsRepository userDetailsRepository,
                           UserDetailsRegistrationService userDetailsRegistrationService) {
        this.userRegistrationService = userRegistrationService;
        this.updateUserService = updateUserService;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.verificationService = new VerificationService();
        this.userDetailsRegistrationService = userDetailsRegistrationService;
        this.bookMockApi = new BookMockApi();
    }

    /**
     * PUT /admin/{adminID}/banUser/{userID} : Bans the account of a user.
     *
     * @param adminId Numeric ID of the admin that makes the request
     * @param userId  Numeric ID of the user to ban
     * @return 200: User banned successfully
     *      401: User does not have admin privileges
     *      404: User could not be found
     *      500: User account could not be modified
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/admin/{adminID}/banUser/{userID}"
    )
    public ResponseEntity<String> banUser(
        @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminId,
        @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userId) {
        return setBanned(adminId, userId, true);
    }

    /**
     * PUT /admin/{adminID}/unbanUser/{userID} : Unbans the account of a user.
     *
     * @param adminId Numeric ID of the admin that makes the request
     * @param userId  Numeric ID of the user to unban
     * @return 200: User unbanned successfully
     *      401: User does not have admin privileges
     *      404: User could not be found
     *      500: User account could not be modified
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/admin/{adminID}/unbanUser/{userID}"
    )
    public ResponseEntity<String> unbanUser(
        @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminId,
        @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userId) {
        return setBanned(adminId, userId, false);
    }

    private ResponseEntity<String> setBanned(Integer adminId, Integer userId, boolean banned) {
        if (adminId == null || userId == null) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        User admin;
        User toModify;
        try {
            admin = userRegistrationService.getUserById(adminId);
            toModify = userRegistrationService.getUserById(userId);
            if (admin == null || toModify == null) {
                throw new RuntimeException("Users not found");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        if (!new AdminAuthentication(adminId, userRegistrationService).authenticate()) {
            return new ResponseEntity<>("User does not have admin privileges", HttpStatus.UNAUTHORIZED);
        }

        try {
            toModify.setIsBanned(banned);
            userRepository.save(toModify);
            return new ResponseEntity<>("User " + (banned ? "" : "un") + "banned successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("User account could not be modified", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /admin/{adminID}/deleteUser/{userID} : Deletes the account of a user from the user repository.
     *
     * @param adminId Numeric ID of the admin that makes the request
     * @param userId  Numeric ID of the user to delete
     * @return 200: User deleted successfully
     *      401: User does not have admin privileges
     *      404: User could not be found
     *      500: User account could not be deleted
     */
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/admin/{adminID}/deleteUser/{userID}"
    )
    public ResponseEntity<String> deleteUser(
        @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminId,
        @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userId) {
        //First find the two users
        if (adminId == null || userId == null) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        User admin;
        User toDelete;
        try {
            admin = userRegistrationService.getUserById(adminId);
            toDelete = userRegistrationService.getUserById(userId);
            if (admin == null || toDelete == null) {
                throw new RuntimeException("Users not found");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        //Check if user is admin
        Authentication authentication = new AdminAuthentication(adminId, userRegistrationService);
        if (!authentication.authenticate()) {
            return new ResponseEntity<>("User does not have admin privileges", HttpStatus.UNAUTHORIZED);
        }

        //Delete user
        try {
            userRepository.delete(toDelete);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("User account could not be deleted", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST /admin/{adminID}/addBook : Adds a book to the book repository.
     *
     * @param adminId Numeric ID of the admin that makes the request (required)
     * @param book    (required)
     * @return Book added successfully (status code 200)
     *      or User does not have admin privileges (status code 401)
     *      or Book could not be added (status code 500)
     */

    @PostMapping("/admin/{adminID}/addBook")
    public ResponseEntity<Void> adminAdminIdAddBookPost(
        @Parameter(name = "adminID", required = true) @PathVariable("adminID") Integer adminId,
        @Parameter(name = "Book", required = true) @Valid @RequestBody Book book
    ) {
        if (!new AdminAuthentication(adminId, userRegistrationService).authenticate()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (book == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bookMockApi.bookPost(book);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * DELETE /admin/{adminID}/removeBook/{bookID} : Removes a book from the book repository.
     *
     * @param adminId Numeric ID of the admin that makes the request (required)
     * @param bookId  ID of the book to delete (required)
     * @return Book removed successfully (status code 200)
     *      or User does not have admin privileges (status code 401)
     *      or Book could not be found (status code 404)
     *      or Book could not be removed (status code 500)
     */

    @DeleteMapping("/admin/{adminID}/removeBook/{bookID}")
    public ResponseEntity<Void> adminAdminIdRemoveBookBookIdDelete(
        @Parameter(name = "adminID", required = true) @PathVariable("adminID") Integer adminId,
        @Parameter(name = "bookID", required = true) @PathVariable("bookID") Integer bookId
    ) {
        if (!new AdminAuthentication(adminId, userRegistrationService).authenticate()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        bookMockApi.bookBookIdDelete(bookId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * PUT /admin/{adminID}/editBook/{bookID} : Edits a book from the book repository.
     *
     * @param adminId Numeric ID of the admin that makes the request (required)
     * @param bookId  ID of the book to update (required)
     * @param book    (required)
     * @return Book edits saved successfully (status code 200)
     *      or User does not have admin privileges (status code 401)
     *      or Book could not be found (status code 404)
     *      or Book could not be edited (status code 500)
     */
    @Operation(
        operationId = "adminAdminIDEditBookBookIDPut",
        summary = "Edits a book from the book repository",
        tags = {"Admin Operations"},
        responses = {
            @ApiResponse(responseCode = "200", description = "Book edits saved successfully"),
            @ApiResponse(responseCode = "401", description = "User does not have admin privileges"),
            @ApiResponse(responseCode = "404", description = "Book could not be found"),
            @ApiResponse(responseCode = "500", description = "Book could not be edited")
        }
    )
    @PutMapping("/admin/{adminID}/editBook/{bookID}")
    public ResponseEntity<Void> adminAdminIdEditBookBookIdPut(
        @Parameter(name = "adminID", required = true) @PathVariable("adminID") Integer adminId,
        @Parameter(name = "bookID", required = true) @PathVariable("bookID") Integer bookId,
        @Parameter(name = "Book", required = true) @Valid @RequestBody Book book
    ) {
        if (!new AdminAuthentication(adminId, userRegistrationService).authenticate()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        bookMockApi.bookPut(book);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
