package nl.tudelft.sem.template.example.domain.exceptions;

public class NotFoundException extends InputFormatException {
    public NotFoundException(String message) {
        super(message);
    }
}
