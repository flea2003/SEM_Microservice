package nl.tudelft.sem.template.example.domain.exceptions;

import java.io.Serial;

public class UpdateDataException extends InputFormatException {
    @Serial
    private static final long serialVersionUID = 7426959831230487308L;

    public UpdateDataException(String message) {
        super(message);
    }
}
