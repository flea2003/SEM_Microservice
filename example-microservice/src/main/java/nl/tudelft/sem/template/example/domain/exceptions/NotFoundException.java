package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class NotFoundException extends InputFormatException {
    @Serial
    private static final long serialVersionUID = -5353549235861229200L;

    public NotFoundException(String message) {
        super(message);
    }
}
