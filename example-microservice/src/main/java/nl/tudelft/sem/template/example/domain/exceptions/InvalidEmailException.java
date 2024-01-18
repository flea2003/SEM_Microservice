package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class InvalidEmailException extends InputFormatException {
    @Serial
    private static final long serialVersionUID = 6404128059897041649L;

    public InvalidEmailException(String message) {
        super(message);
    }
}
