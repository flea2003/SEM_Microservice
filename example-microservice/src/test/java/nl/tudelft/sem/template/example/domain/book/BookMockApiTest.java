package nl.tudelft.sem.template.example.domain.book;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookMockApiTest {

    private static BookMockApi mock;

    @BeforeAll
    static void setup() {
       mock = new BookMockApi();
    }

    @Test
    void addBookNullTest(){
        Book bookToAdd = null;

        ResponseEntity<Void> result = mock.bookPost(bookToAdd);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void addBookTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");


        ResponseEntity<Void> result = mock.bookPost(bookToAdd);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mock.books.get(0), bookToAdd );
    }

    @Test
    void getBookTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        mock.bookPost(bookToAdd);

        ResponseEntity<Book> result = mock.bookBookIdGet(1);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(result.getBody(), bookToAdd );
    }

    @Test
    void getBookNullTest(){
        ResponseEntity<Book> result = mock.bookBookIdGet(null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void DeleteBookTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        mock.bookPost(bookToAdd);
        assertEquals(mock.books.get(0), bookToAdd);

        ResponseEntity<Void> result = mock.bookBookIdDelete(1);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mock.books.size(), 0);
    }

    @Test
    void DeleteBookBadRequestTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        mock.bookPost(bookToAdd);
        assertEquals(mock.books.get(0), bookToAdd);

        ResponseEntity<Void> result = mock.bookBookIdDelete(5);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(mock.books.get(0), bookToAdd);
    }

    @Test
    void UpdateBookTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        mock.bookPost(bookToAdd);
        assertEquals(mock.books.get(0), bookToAdd);

        Book bookToUpdate = new Book(1, "Not Anymore", "", authors, "Comedy");

        ResponseEntity<Void> result = mock.bookPut(bookToUpdate);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(mock.books.get(0), bookToUpdate);
    }

    @Test
    void UpdateBookBadRequestTest(){
        String[] authors = new String[2];
        authors[0] = "Han";
        authors[1] = "Jan";
        Book bookToAdd = new Book(1, "New Book", "", authors, "Comedy");

        mock.bookPost(bookToAdd);
        assertEquals(mock.books.get(0), bookToAdd);

        Book bookToUpdate = new Book(5, "Not Anymore", "", authors, "Sci-fi");

        ResponseEntity<Void> result = mock.bookPut(bookToUpdate);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(mock.books.get(0), bookToAdd);
    }
}
