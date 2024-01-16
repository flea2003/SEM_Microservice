package nl.tudelft.sem.template.example.domain.exceptions;

public class MalformedBodyException extends InputFormatException {
    public MalformedBodyException(String message) {
        super(message);
    }
}
