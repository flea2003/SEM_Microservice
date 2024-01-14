package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import java.util.regex.Pattern;

public class EmailFormatValidator<T extends UserPostRequest> extends BaseValidator<T> {
    @Override
    public boolean handle(T user) throws InvalidEmailException, AlreadyExistsException, InvalidUsernameException, MalformedBodyException {
        String email = user.getEmail();
        if(!Pattern.matches("^[a-zA-Z][a-zA-Z0-9-_]*@[a-zA-Z][a-zA-Z0-9-_]*.[a-zA-Z][a-zA-Z0-9-_]{1,3}", email))
            throw new InvalidEmailException("Email format incorrect!");
        return super.checkNext(user);
    }
}
