package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class InvalidUserDetailsException extends InputFormatException {
    @Serial
    private static final long serialVersionUID = -1702817331940069490L;

    public InvalidUserDetailsException() {
        super("New user details are invalid");
    }

    public InvalidUserDetailsException(String message) {
        super(message);
    }
}
