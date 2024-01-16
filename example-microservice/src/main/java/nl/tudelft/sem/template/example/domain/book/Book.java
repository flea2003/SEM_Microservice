package nl.tudelft.sem.template.example.domain.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Book {

    private int isbn;
    private String title;
    private String series;
    private String[] authors;
    private String[] genres;

    public Book(int id, String title, String series, String[] authors, String... genres) {
        this.isbn = id;
        this.title = title;
        this.series = series;
        this.authors = authors;
        this.genres = genres;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (getClass() == other.getClass() && this.isbn == ((Book) other).isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(41 /*Special number*/, isbn);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("class Book {\n")
                .append("    id: ").append(isbn).append("\n")
                .append("    title: ").append(title).append("\n")
                .append("    series: ").append(series).append("\n")
                .append("    authors: ").append(String.join(", ", authors)).append("\n")
                .append("    genres: ").append(String.join(", ", genres)).append("\n")
                .append("}");
        return sb.toString();
    }

}
