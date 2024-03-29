/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.6.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package nl.tudelft.sem.template.example.domain.book;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.List;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-01-09T14:33:54.623406+01:00[Europe/Amsterdam]")
@Validated
@Tag(name = "book", description = "Books are the essential entities and represent books in real life.")
public class BookMockApi {

    @Getter
    List<Book> books;

    public BookMockApi() {
        this.books = new ArrayList<>();
    }

    /**
     * DELETE /book/{bookId} : Delete a book
     * Deletes the book with specified id, if it exists.
     *
     * @param bookId Id of book to delete (required)
     * @return OK (status code 200)
     *         or Invalid id given (status code 400)
     *         or Book not found (status code 404)
     */
    @Operation(
            operationId = "bookBookIdDelete",
            summary = "Delete a book",
            description = "Deletes the book with specified id, if it exists.",
            tags = { "book" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Invalid id given"),
                    @ApiResponse(responseCode = "404", description = "Book not found")
            }
    )
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/book/{bookId}"
    )
    public ResponseEntity<Void> bookBookIdDelete(
            @Parameter(name = "bookId", description = "Id of book to delete", required = true, in = ParameterIn.PATH) @PathVariable("bookId") Integer bookId
    ) {
        for(Book book : books) {
            if(book.getIsbn() == bookId) {
                books.remove(book);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }


    /**
     * GET /book/{bookId} : Find a book
     * Finds and returns the book with specified id, if it exists.
     *
     * @param bookId Id of book to return (required)
     * @return OK (status code 200)
     *         or Invalid id given (status code 400)
     *         or Book not found (status code 404)
     */
    @Operation(
            operationId = "bookBookIdGet",
            summary = "Find a book",
            description = "Finds and returns the book with specified id, if it exists.",
            tags = { "book" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
                    }),
                    @ApiResponse(responseCode = "400", description = "Invalid id given"),
                    @ApiResponse(responseCode = "404", description = "Book not found")
            }
    )
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/book/{bookId}",
            produces = { "application/json" }
    )
    public ResponseEntity<Book> bookBookIdGet(
            @Parameter(name = "bookId", description = "Id of book to return", required = true, in = ParameterIn.PATH) @PathVariable("bookId") Integer bookId
    ) {
        Book book = null;

        if(bookId != null) {
            for(Book tempBook : books){
                if(tempBook.getIsbn() == bookId){
                    book = tempBook;
                    return new ResponseEntity<>(book,HttpStatus.OK);
                }
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * POST /book : Add a new book
     * Creates a new book with all attributes except id, and adds it to the system. Returns the newly created book with generated id.
     *
     * @param book  (optional)
     * @return OK (status code 200)
     *         or Invalid format of parameters (status code 400)
     */
    @Operation(
            operationId = "bookPost",
            summary = "Add a new book",
            description = "Creates a new book with all attributes except id, and adds it to the system. Returns the newly created book with generated id.",
            tags = { "book" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK", content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
                    }),
                    @ApiResponse(responseCode = "400", description = "Invalid format of parameters")
            }
    )
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/book",
            produces = { "application/json" },
            consumes = { "application/json" }
    )
    public ResponseEntity<Void> bookPost(
            @Parameter(name = "Book", description = "") @Valid @RequestBody(required = false) Book book
    ) {
        if(book != null) {
            books.add(book);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    /**
     * PUT /book : Update a book
     * Changes all attributes of a book with a certain id, except the id.
     *
     * @param book  (optional)
     * @return OK (status code 200)
     *         or Invalid id or format of parameters (status code 400)
     *         or Book not found (status code 404)
     */
    @Operation(
            operationId = "bookPut",
            summary = "Update a book",
            description = "Changes all attributes of a book with a certain id, except the id.",
            tags = { "book" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "400", description = "Invalid id or format of parameters"),
                    @ApiResponse(responseCode = "404", description = "Book not found")
            }
    )
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/book",
            consumes = { "application/json" }
    )
    public ResponseEntity<Void> bookPut(
            @Parameter(name = "Book", description = "") @Valid @RequestBody(required = false) Book book
    ) {
        for(Book tempBook : books) {
            if(tempBook.getIsbn() == book.getIsbn()) {
                books.remove(tempBook);
                books.add(book);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
