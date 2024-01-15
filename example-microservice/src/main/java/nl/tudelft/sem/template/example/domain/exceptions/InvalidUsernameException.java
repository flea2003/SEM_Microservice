package nl.tudelft.sem.template.example.domain.exceptions;

public class InvalidUsernameException extends Exception {
    public InvalidUsernameException(String message) {
        super(message);
    }
}
