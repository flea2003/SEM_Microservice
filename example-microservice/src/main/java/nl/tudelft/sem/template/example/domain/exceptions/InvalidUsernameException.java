package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class InvalidUsernameException extends InputFormatException {
    @Serial
    private static final long serialVersionUID = 7763184548400889203L;

    public InvalidUsernameException(String message) {
        super(message);
    }
}
