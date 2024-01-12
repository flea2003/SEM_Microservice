package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.*;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public class NoSameEmailUserValidator extends BaseUserPostRequestValidator {
    private final UserRegistrationService userRegistrationService;

    public NoSameEmailUserValidator(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @Override
    public boolean handle(UserPostRequest user) throws AlreadyExistsException, InvalidUsernameException, MalformedBodyException, InvalidEmailException {
        String email = user.getEmail();
        if (userRegistrationService.getUserByEmail(user.getEmail()) != null) {
            throw new AlreadyExistsException("User with email already exists");
        }
        return super.checkNext(user);
    }
}
