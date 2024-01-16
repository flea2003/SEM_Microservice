package nl.tudelft.sem.template.example.handlers.details;

import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;
import nl.tudelft.sem.template.example.domain.exceptions.NotFoundException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.handlers.BaseValidator;

public class RequestUserValidator<T extends EditUserRequestParameters> extends BaseValidator<T> {
    private final UserRegistrationService userRegistrationService;
    public RequestUserValidator(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @Override
    public boolean handle(T request) throws InputFormatException {
        User user = userRegistrationService.getUserById(request.getUserID());
        if(user == null)
            throw new NotFoundException("User could not be found");
        return super.checkNext(request);
    }
}
