package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class InputFormatException extends Exception {
    @Serial
    private static final long serialVersionUID = 2915843853740592962L;

    public InputFormatException(String message) {
        super(message);
    }
}
