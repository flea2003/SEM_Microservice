package nl.tudelft.sem.template.example.domain.exceptions;

public class InvalidEmailException extends InputFormatException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
