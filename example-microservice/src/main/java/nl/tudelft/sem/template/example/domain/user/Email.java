package nl.tudelft.sem.template.example.domain.user;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Email {
    private final transient String userEmail;

    /**
     * Email constructor.
     *
     * @param userEmail The string to validate and assemble email from
     */
    public Email(String userEmail) {
        if (!validate(userEmail)) {
            this.userEmail = null;
        } else {
            this.userEmail = userEmail;
        }
    }

    private boolean validate(String userEmail) {
        return Pattern.matches("^[a-zA-Z][a-zA-Z0-9-_]*@[a-zA-Z][a-zA-Z0-9-_]*.[a-zA-Z][a-zA-Z0-9-_]{1,3}", userEmail);
    }

    @Override
    public String toString() {
        return userEmail;
    }

    @JsonValue
    public String getEmail() {
        return userEmail;
    }
}
