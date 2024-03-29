package nl.tudelft.sem.template.example.handlers.userCreation;

import nl.tudelft.sem.template.example.domain.exceptions.*;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.handlers.BaseValidator;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public class NoSameEmailUserValidator<T extends UserPostRequest> extends BaseValidator<T> {
    private final UserRegistrationService userRegistrationService;

    public NoSameEmailUserValidator(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @Override
    public boolean handle(T user) throws InputFormatException {
        String email = user.getEmail();
        if (userRegistrationService.getUserByEmail(user.getEmail()) != null) {
            throw new AlreadyExistsException("User with email already exists");
        }
        return super.checkNext(user);
    }
}
