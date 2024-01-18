package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class AlreadyHavePermissionsException extends Exception {

    @Serial
    private static final long serialVersionUID = -2548158830040751985L;

    public AlreadyHavePermissionsException() {
        super("You already have those permissions");
    }

    public AlreadyHavePermissionsException(String message) {
        super(message);
    }
}
