package nl.tudelft.sem.template.example.domain.book;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    @Test
    void testCreateBook() {
        Book book = new Book(1, "Some Title", "Some Series", new String[] {"Some Author"}, "Some Genre");
        assertEquals(1, book.getId());
        assertEquals("Some Title", book.getTitle());
        assertEquals("Some Series", book.getSeries());
        assertEquals("Some Author", book.getAuthors()[0]);
        assertEquals("Some Genre", book.getGenres()[0]);
    }

    @Test
    void testBookEquals() {
        Book book0 = new Book(1, "Some Title", "Some Series", new String[] {"Some Author"}, "Some Genre");
        Book book1 = new Book(1, "Another", "Series 2", new String[] {"Some Author"}, "Some Genre");
        Book book2 = new Book(2, "Some Title", "Some Series", new String[] {"Some Author"}, "Some Genre");

        assertEquals(book0, book1);
        assertNotEquals(book0, book2);
    }

    @Test
    void testBookHashCode() {
        Book book0 = new Book(1, "Some Title", "Some Series", new String[] {"Some Author"}, "Some Genre");
        Book book1 = new Book(1, "Another", "Series 2", new String[] {"Some Author"}, "Some Genre");

        assertEquals(book0.hashCode(), book1.hashCode());
        assertNotEquals(0, book0.hashCode());
    }

    @Test
    void testBookToString() {
        Book book = new Book(1, "Some Title", "Some Series", new String[] {"Author1", "Author2"}, "Some Genre");
        assertEquals("class Book {\n"
            + "    id: 1\n"
            + "    title: Some Title\n"
            + "    series: Some Series\n"
            + "    authors: Author1, Author2\n"
            + "    genres: Some Genre\n"
                + "}", book.toString());
    }

}
