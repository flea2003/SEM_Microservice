package nl.tudelft.sem.template.example.domain.user;

/**
 * Exception to indicate the NetID is already in use.
 */
public class InvalidUserException extends Exception {
    static final long serialVersionUID = -3387516993124229948L;

    public InvalidUserException() {
        super("Username or password was invalid");
    }
}
