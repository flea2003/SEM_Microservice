package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class AlreadyExistsException extends InputFormatException {
    @Serial
    private static final long serialVersionUID = 7773315391889480226L;

    public AlreadyExistsException(String message) {
        super(message);
    }
}
