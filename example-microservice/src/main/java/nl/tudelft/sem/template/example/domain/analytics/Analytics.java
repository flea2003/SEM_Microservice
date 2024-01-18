package nl.tudelft.sem.template.example.domain.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Analytics {

    @JsonProperty("id")
    private int id;

    @JsonProperty("popular_genres")
    private List<String> popularGenres;

    @JsonProperty("no_logins")
    private int noLogins;

    /**
     * Creates a new Analytics instance with the given fields.
     *
     * @param id the id of this analytics object
     * @param popularGenres the popular genres according to the analysis
     * @param noLogins the number of logins made
     */
    public Analytics(int id, List<String> popularGenres, int noLogins) {
        assert popularGenres != null : "popularGenres must not be null";
        this.id = id;
        this.popularGenres = popularGenres;
        this.noLogins = noLogins;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        Analytics analytics = (Analytics) other;
        return this.id == analytics.id
                && this.popularGenres.equals(analytics.popularGenres)
                && this.noLogins == analytics.noLogins;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.popularGenres, this.noLogins);
    }

    @Override
    public String toString() {
        return "Analytics:\n"
                + "\tid = " + this.id + "\n"
                + "\tpopularGenres = " + String.join(", ", this.popularGenres) + "\n"
                + "\tnoLogins = " + this.noLogins;
    }

}
