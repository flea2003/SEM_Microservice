package nl.tudelft.sem.template.example.domain.book;

import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Book {

    private int id;
    private String title;
    private String series;
    private String[] authors;
    private String[] genres;

    /**
     * Constructor for book class.
     *
     * @param id id
     * @param title title
     * @param series series
     * @param authors authors
     * @param genres genres
     */
    public Book(int id, String title, String series, String[] authors, String... genres) {
        this.id = id;
        this.title = title;
        this.series = series;
        this.authors = authors;
        this.genres = genres;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (getClass() == other.getClass() && this.id == ((Book) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(41 /*Special number*/, id);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("class Book {\n")
                .append("    id: ").append(id).append("\n")
                .append("    title: ").append(title).append("\n")
                .append("    series: ").append(series).append("\n")
                .append("    authors: ").append(String.join(", ", authors)).append("\n")
                .append("    genres: ").append(String.join(", ", genres)).append("\n")
                .append("}");
        return sb.toString();
    }

}
