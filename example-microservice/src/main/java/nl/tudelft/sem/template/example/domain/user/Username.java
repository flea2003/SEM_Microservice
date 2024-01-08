package nl.tudelft.sem.template.example.domain.user;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import java.util.regex.Pattern;
import lombok.Data;


@EqualsAndHashCode
@Data
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

    @JsonValue
    public String getUsername() {
        return username;
    }
}
