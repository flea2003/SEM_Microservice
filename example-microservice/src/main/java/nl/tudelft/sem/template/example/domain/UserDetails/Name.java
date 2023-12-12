package nl.tudelft.sem.template.example.domain.UserDetails;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Data;


import java.io.Serializable;
import java.util.regex.Pattern;

@EqualsAndHashCode
@Data
public class Name implements Serializable {
    private String name;

    /**
     * Empty constructor
     */
    public Name(){

    }

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

    @JsonValue
    public String getValue() {
        return name;
    }

    private boolean validateName(String name) {
        return Pattern.matches("^[a-zA-Z]+ [a-zA-Z]+$", name);
    }

    @Override
    public String toString() {
        return name;
    }
}