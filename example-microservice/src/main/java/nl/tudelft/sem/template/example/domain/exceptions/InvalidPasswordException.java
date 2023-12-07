package nl.tudelft.sem.template.example.domain.exceptions;

public class InvalidPasswordException extends Exception {

    public InvalidPasswordException() {
        super("Invalid password!");
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}
