package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class InvalidPasswordException extends Exception {

    @Serial
    private static final long serialVersionUID = -8135896350178429283L;

    public InvalidPasswordException() {
        super("Invalid password!");
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}
