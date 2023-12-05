package nl.tudelft.sem.template.example.domain.user;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Email {
    private final transient String email;

    /**
     * Email constructor.
     *
     * @param email The string to validate and assemble email from
     */
    public Email(String email) {
        if (!validate(email)) {
            this.email = null;
        } else {
            this.email = email;
        }
    }

    private boolean validate(String email) {
        return Pattern.matches("^[a-zA-Z][a-zA-Z0-9-_]*@[a-zA-Z][a-zA-Z0-9-_]*.[a-zA-Z][a-zA-Z0-9-_]{1,3}", email);
    }

    @Override
    public String toString() {
        return email;
    }
}
