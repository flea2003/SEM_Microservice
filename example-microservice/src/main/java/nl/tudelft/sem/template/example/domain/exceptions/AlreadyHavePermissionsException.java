package nl.tudelft.sem.template.example.domain.exceptions;

public class AlreadyHavePermissionsException extends Exception{

    public AlreadyHavePermissionsException() {
        super("You already have those permissions");
    }

    public AlreadyHavePermissionsException(String message) {
        super(message);
    }
}
