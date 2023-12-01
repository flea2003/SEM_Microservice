package nl.tudelft.sem.template.example.domain.user;

import lombok.EqualsAndHashCode;
import java.util.regex.Pattern;

@EqualsAndHashCode
public class Username {
    private final transient String username;

    public Username(String username) {
        if(!validate(username))
            this.username = null;
        else
            this.username = username;
    }

    private boolean validate(String username) {
        return Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*",username);
    }

    @Override
    public String toString() {
        return username;
    }
}
