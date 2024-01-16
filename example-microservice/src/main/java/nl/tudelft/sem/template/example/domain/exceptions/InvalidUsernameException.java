package nl.tudelft.sem.template.example.domain.exceptions;

public class InvalidUsernameException extends InputFormatException {
    public InvalidUsernameException(String message) {
        super(message);
    }
}
