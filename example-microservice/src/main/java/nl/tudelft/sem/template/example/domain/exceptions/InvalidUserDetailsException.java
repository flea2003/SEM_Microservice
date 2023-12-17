package nl.tudelft.sem.template.example.domain.exceptions;

public class InvalidUserDetailsException extends Exception {
    public InvalidUserDetailsException() {
        super("New user details are invalid");
    }

    public InvalidUserDetailsException(String message) {
        super(message);
    }
    public InvalidUserDetailsException(Exception e) {
        super(e);
    }
}
