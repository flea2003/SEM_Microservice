package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class MalformedBodyException extends InputFormatException {
    @Serial
    private static final long serialVersionUID = 7656553140248222280L;

    public MalformedBodyException(String message) {
        super(message);
    }
}
