package nl.tudelft.sem.template.example.handlers.userCreation;

import nl.tudelft.sem.template.example.domain.exceptions.*;
import nl.tudelft.sem.template.example.domain.user.Email;
import nl.tudelft.sem.template.example.handlers.BaseValidator;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public class EmailFormatValidator<T extends UserPostRequest> extends BaseValidator<T> {
    @Override
    public boolean handle(T user) throws InputFormatException {
        Email email = new Email(user.getEmail());
        if(email.getEmail() == null) {
            throw new InvalidEmailException("Email format incorrect!");
        }
        return super.checkNext(user);
    }
}
