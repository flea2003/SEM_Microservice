package nl.tudelft.sem.template.example.handlers.creation;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.handlers.BaseValidator;
import nl.tudelft.sem.template.example.models.UserPostRequest;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class NoSameEmailUserValidator<T extends UserPostRequest> extends BaseValidator<T> {
    private final transient UserRegistrationService userRegistrationService;

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
