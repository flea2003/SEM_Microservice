package nl.tudelft.sem.template.example.handlers.string;
import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public interface UserPostRequestValidator {
    void setNextOperation(UserPostRequestValidator next);
    void link(UserPostRequestValidator... others);
    boolean handle(UserPostRequest user) throws InvalidUsernameException, MalformedBodyException, AlreadyExistsException, InvalidEmailException;
}
