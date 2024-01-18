package nl.tudelft.sem.template.example.domain.user;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode
@Data
@SuppressWarnings("PMD.NullAssignment")
public class Username {
    private final transient String usersname;

    /**
     * Constructor for Username class.
     *
     * @param usersname username
     */
    public Username(String usersname) {
        if (!validate(usersname)) {
            this.usersname = null;
        } else {
            this.usersname = usersname;
        }
    }

    private boolean validate(String usersname) {
        return Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*", usersname);
    }

    @Override
    public String toString() {
        return usersname;
    }

    @JsonValue
    public String getUsername() {
        return usersname;
    }
}
