package nl.tudelft.sem.template.example.handlers.creation;

import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.handlers.BaseValidator;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public class NullOrEmptyFieldsValidator<T extends UserPostRequest> extends BaseValidator<T> {
    @Override
    public boolean handle(T user) throws InputFormatException {
        if (user == null
            || user.getEmail() == null || user.getEmail().isEmpty()
            || user.getPassword() == null || user.getPassword().isEmpty()
            || user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new MalformedBodyException("Request body is malformed");
        }
        return super.checkNext(user);
    }
}
