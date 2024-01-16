package nl.tudelft.sem.template.example.domain.exceptions;

public class AlreadyExistsException extends Exception {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
