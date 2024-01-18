package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class InvalidPermissionsException extends Exception {

    @Serial
    private static final long serialVersionUID = -7340470885031325723L;

    public InvalidPermissionsException() {
        super("You do not have the permissions to do this!");
    }

    public InvalidPermissionsException(String message) {
        super(message);
    }
}
