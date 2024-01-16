package nl.tudelft.sem.template.example.domain.exceptions;

public class AlreadyExistsException extends InputFormatException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
