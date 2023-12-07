package nl.tudelft.sem.template.example.domain.exceptions;

/**
 * Exception to indicate the username/email of the user was invalid.
 */
public class InvalidUserException extends Exception {
    static final long serialVersionUID = -3387516993124229948L;

    public InvalidUserException() {
        super("Username or email was invalid");
    }
}
