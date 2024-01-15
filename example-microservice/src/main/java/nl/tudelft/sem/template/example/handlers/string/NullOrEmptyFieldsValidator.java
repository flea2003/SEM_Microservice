package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public class NullOrEmptyFieldsValidator<T extends UserPostRequest> extends BaseValidator<T> {
    @Override
    public boolean handle(T user) throws MalformedBodyException, AlreadyExistsException, InvalidUsernameException, InvalidEmailException {
        if(user == null ||
        user.getEmail() == null || user.getEmail().isEmpty() ||
        user.getPassword() == null || user.getPassword().isEmpty() ||
        user.getUsername() == null || user.getUsername().isEmpty())
            throw new MalformedBodyException("Request body is malformed");
        return super.checkNext(user);
    }
}
