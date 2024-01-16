package nl.tudelft.sem.template.example.handlers.userCreation;

import nl.tudelft.sem.template.example.domain.exceptions.*;
import nl.tudelft.sem.template.example.domain.user.Username;
import nl.tudelft.sem.template.example.handlers.BaseValidator;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public class UsernameFormatValidator<T extends UserPostRequest> extends BaseValidator<T> {
    @Override
    public boolean handle(T user) throws InputFormatException {
        Username username = new Username(user.getUsername());
        if(username.getUsername() == null) {
            throw new InvalidUsernameException("Username format incorrect!");
        }
        return super.checkNext(user);
    }
}
