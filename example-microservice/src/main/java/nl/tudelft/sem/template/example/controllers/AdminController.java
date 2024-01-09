package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.book.BookMockApi;
import nl.tudelft.sem.template.example.domain.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;

@RestController
public class AdminController {

    BookMockApi bookMockApi;
    UserRegistrationService userRegistrationService;

    @Autowired
    public AdminController(UserRegistrationService userRegistrationService) {
        this.bookMockApi = new BookMockApi();
        this.userRegistrationService = userRegistrationService;
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
        User admin = userRegistrationService.getUserById(adminID);
        if(admin.getIsAdmin()) {
            if(book != null) {
                bookMockApi.bookPost(book);
                return new ResponseEntity<>(HttpStatus.OK);
            }

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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

        User admin = userRegistrationService.getUserById(adminID);

        if(admin.getIsAdmin()) {
            bookMockApi.bookBookIdDelete(bookID);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

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

        User admin = userRegistrationService.getUserById(adminID);

        if(admin.getIsAdmin()) {
            bookMockApi.bookPut(book);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
