package nl.tudelft.sem.template.example.domain.exceptions;

public class InvalidUserDetailsException extends InputFormatException {
    public InvalidUserDetailsException() {
        super("New user details are invalid");
    }

    public InvalidUserDetailsException(String message) {
        super(message);
    }
}
