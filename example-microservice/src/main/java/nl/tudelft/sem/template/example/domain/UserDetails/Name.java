package nl.tudelft.sem.template.example.domain.UserDetails;

import lombok.EqualsAndHashCode;

import java.util.regex.Pattern;

@EqualsAndHashCode
public class Name {
    private final transient String name;

    /**
     * Name constructor.
     *
     * @param name The string to validate and assemble email from
     */
    public Name(String name) {
        if (!validateName(name)) {
            this.name = "";
        } else {
            this.name = name;
        }
    }

    private boolean validateName(String name) {
        return Pattern.matches("^[a-zA-Z]+ [a-zA-Z]+$", name);
    }

    @Override
    public String toString() {
        return name;
    }
}