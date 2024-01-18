package nl.tudelft.sem.template.example.handlers.details;

import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
import nl.tudelft.sem.template.example.domain.exceptions.UpdateDataException;
import nl.tudelft.sem.template.example.domain.userdetails.UpdateUserDetailsService;
import nl.tudelft.sem.template.example.handlers.BaseValidator;

public class UserDetailsValidator<T extends EditUserRequestParameters> extends BaseValidator<T> {
    private final transient UpdateUserDetailsService updateUserDetailsService;

    public UserDetailsValidator(UpdateUserDetailsService updateUserDetailsService) {
        this.updateUserDetailsService = updateUserDetailsService;
    }

    @Override
    public boolean handle(T request) throws InputFormatException {
        try {
            updateUserDetailsService.updateUserDetails(request.getUserId(), request.getUserDetails());
        } catch (InvalidUserDetailsException e) {
            throw new InvalidUserDetailsException(e.getMessage());
        } catch (Exception e) {
            throw new UpdateDataException("Unauthorised changes to the user");
        }
        return super.checkNext(request);
    }
}
