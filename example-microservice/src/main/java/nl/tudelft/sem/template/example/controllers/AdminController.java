package nl.tudelft.sem.template.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Getter;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.book.BookMockApi;
import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.strategy.AdminAuthentication;
import nl.tudelft.sem.template.example.strategy.Authentication;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
public class AdminController {
    UserRegistrationService userRegistrationService;
    UserDetailsRegistrationService userDetailsRegistrationService;
    @Getter
    BookMockApi bookMockApi;
    UserRepository userRepository;
    UserDetailsRepository userDetailsRepository;
    VerificationService verificationService;
    UpdateUserService updateUserService;

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
     * @param adminID Numeric ID of the admin that makes the request
     * @param userID Numeric ID of the user to ban
     * @return 200: User banned successfully
     *         401: User does not have admin privileges
     *         404: User could not be found
     *         500: User account could not be modified
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/admin/{adminID}/banUser/{userID}"
    )
    public ResponseEntity<String> banUser(
            @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID) {
        return setBanned(adminID, userID, true);
    }

    /**
     * PUT /admin/{adminID}/unbanUser/{userID} : Unbans the account of a user.
     *
     * @param adminID Numeric ID of the admin that makes the request
     * @param userID Numeric ID of the user to unban
     * @return 200: User unbanned successfully
     *         401: User does not have admin privileges
     *         404: User could not be found
     *         500: User account could not be modified
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/admin/{adminID}/unbanUser/{userID}"
    )
    public ResponseEntity<String> unbanUser(
            @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID) {
        return setBanned(adminID, userID, false);
    }

    private ResponseEntity<String> setBanned(Integer adminID, Integer userID, boolean banned) {
        if (adminID == null || userID == null)
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);

        User admin, toModify;
        try {
            admin = userRegistrationService.getUserById(adminID);
            toModify = userRegistrationService.getUserById(userID);
            if (admin == null || toModify == null)
                throw new RuntimeException("Users not found");
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        if(!new AdminAuthentication(adminID, userRegistrationService).authenticate())
            return new ResponseEntity<>("User does not have admin privileges", HttpStatus.UNAUTHORIZED);

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
     * @param adminID Numeric ID of the admin that makes the request
     * @param userID Numeric ID of the user to delete
     * @return 200: User deleted successfully
     *         401: User does not have admin privileges
     *         404: User could not be found
     *         500: User account could not be deleted
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/admin/{adminID}/deleteUser/{userID}"
    )
    public ResponseEntity<String> deleteUser(
            @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID) {
        //First find the two users
        if (adminID == null || userID == null)
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);

        User admin, toDelete;
        try {
            admin = userRegistrationService.getUserById(adminID);
            toDelete = userRegistrationService.getUserById(userID);
            if (admin == null || toDelete == null)
                throw new RuntimeException("Users not found");
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        //Check if user is admin
        Authentication authentication = new AdminAuthentication(adminID, userRegistrationService);
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
     * POST /admin/{adminID}/addBook : Adds a book to the book repository
     *
     * @param adminID Numeric ID of the admin that makes the request (required)
     * @param book  (required)
     * @return Book added successfully (status code 200)
     *         or User does not have admin privileges (status code 401)
     *         or Book could not be added (status code 500)
     */

    @PostMapping("/admin/{adminID}/addBook")
    public ResponseEntity<Void> adminAdminIDAddBookPost(
            @Parameter(name = "adminID", required = true) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "Book", required = true) @Valid @RequestBody Book book
    ) {
        if(!new AdminAuthentication(adminID, userRegistrationService).authenticate())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        if(book == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String bookToJson = mapper.writeValueAsString(book);
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/book"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bookToJson))
                    .build();
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<>(HttpStatus.valueOf(httpResponse.statusCode()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // If this point has been reached, something went wrong.
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DELETE /admin/{adminID}/removeBook/{bookID} : Removes a book from the book repository
     *
     * @param adminID Numeric ID of the admin that makes the request (required)
     * @param bookID ID of the book to delete (required)
     * @return Book removed successfully (status code 200)
     *         or User does not have admin privileges (status code 401)
     *         or Book could not be found (status code 404)
     *         or Book could not be removed (status code 500)
     */

    @DeleteMapping("/admin/{adminID}/removeBook/{bookID}")
    public ResponseEntity<Void> adminAdminIDRemoveBookBookIDDelete(
            @Parameter(name = "adminID", required = true) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "bookID", required = true) @PathVariable("bookID") Integer bookID
    ) {
        if(!new AdminAuthentication(adminID, userRegistrationService).authenticate())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/book/" + bookID))
                    .DELETE()
                    .build();
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<>(HttpStatus.valueOf(httpResponse.statusCode()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // If this point has been reached, something went wrong.
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * PUT /admin/{adminID}/editBook/{bookID} : Edits a book from the book repository
     *
     * @param adminID Numeric ID of the admin that makes the request (required)
     * @param bookID ID of the book to update (required)
     * @param book  (required)
     * @return Book edits saved successfully (status code 200)
     *         or User does not have admin privileges (status code 401)
     *         or Book could not be found (status code 404)
     *         or Book could not be edited (status code 500)
     */
    @Operation(
            operationId = "adminAdminIDEditBookBookIDPut",
            summary = "Edits a book from the book repository",
            tags = { "Admin Operations" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Book edits saved successfully"),
                    @ApiResponse(responseCode = "401", description = "User does not have admin privileges"),
                    @ApiResponse(responseCode = "404", description = "Book could not be found"),
                    @ApiResponse(responseCode = "500", description = "Book could not be edited")
            }
    )
    @PutMapping("/admin/{adminID}/editBook/{bookID}")
    public ResponseEntity<Void> adminAdminIDEditBookBookIDPut(
            @Parameter(name = "adminID", required = true) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "bookID", required = true) @PathVariable("bookID") Integer bookID,
            @Parameter(name = "Book", required = true) @Valid @RequestBody Book book
    ) {
        if(!new AdminAuthentication(adminID, userRegistrationService).authenticate())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String bookToJson = mapper.writeValueAsString(book);
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/book"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(bookToJson))
                    .build();
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<>(HttpStatus.valueOf(httpResponse.statusCode()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // If this point has been reached, something went wrong.
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
