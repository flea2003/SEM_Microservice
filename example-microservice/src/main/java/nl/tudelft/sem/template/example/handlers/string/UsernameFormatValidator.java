package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import java.util.regex.Pattern;

public class UsernameFormatValidator extends BaseUserPostRequestValidator {
    @Override
    public boolean handle(UserPostRequest user) throws InvalidUsernameException, AlreadyExistsException, MalformedBodyException, InvalidEmailException {
        String username = user.getUsername();
        if(!Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*", username))
            throw new InvalidUsernameException("Username format incorrect!");
        return super.checkNext(user);
    }
}
