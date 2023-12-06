package nl.tudelft.sem.template.example.domain.exceptions;

public class InvalidPermissionsException extends Exception{

    public InvalidPermissionsException() {
        super("You do not have the permissions to do this!");
    }

    public InvalidPermissionsException(String message) {
        super(message);
    }
}
