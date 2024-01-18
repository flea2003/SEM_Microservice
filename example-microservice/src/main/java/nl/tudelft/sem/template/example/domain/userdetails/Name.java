package nl.tudelft.sem.template.example.domain.userdetails;

import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
public class Name implements Serializable {
    @Serial
    private static final long serialVersionUID = -8888327097015402324L;
    private String realName;

    /**
     * Empty constructor.
     */
    public Name() {}

    /**
     * realName constructor.
     *
     * @param realName The string to validate and assemble email from
     */
    public Name(String realName) {
        if (!validaterealName(realName)) {
            this.realName = "";
        } else {
            this.realName = realName;
        }
    }

    @JsonValue
    public String getValue() {
        return realName;
    }

    private boolean validaterealName(String realName) {
        return Pattern.matches("^[a-zA-Z]+ [a-zA-Z]+$", realName);
    }

    @Override
    public String toString() {
        return realName;
    }
}