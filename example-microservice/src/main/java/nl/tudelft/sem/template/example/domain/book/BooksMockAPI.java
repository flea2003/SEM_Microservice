package nl.tudelft.sem.template.example.domain.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BooksMockAPI {

    List<Book> books;

    public BooksMockAPI() {
        books = new ArrayList<>();
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooksMockAPI that = (BooksMockAPI) o;
        return Objects.equals(books, that.books);
    }

    @Override
    public int hashCode() {
        return Objects.hash(books);
    }

    public void addBook(Book book) {
        if(book != null){
            books.add(book);
        }
    }
}
