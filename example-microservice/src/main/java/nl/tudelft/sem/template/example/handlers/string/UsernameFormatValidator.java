package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.domain.user.Username;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import java.util.regex.Pattern;

public class UsernameFormatValidator<T extends UserPostRequest> extends BaseValidator<T> {
    @Override
    public boolean handle(T user) throws InvalidUsernameException, AlreadyExistsException, MalformedBodyException, InvalidEmailException {
        Username username = new Username(user.getUsername());
        if(username.getUsername() == null) {
            throw new InvalidUsernameException("Username format incorrect!");
        }
        return super.checkNext(user);
    }
}
