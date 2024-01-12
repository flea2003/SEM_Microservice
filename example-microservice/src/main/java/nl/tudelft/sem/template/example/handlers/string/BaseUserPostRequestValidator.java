package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.models.UserPostRequest;

public abstract class BaseUserPostRequestValidator implements UserPostRequestValidator {
    private UserPostRequestValidator next;

    @Override
    public void setNextOperation(UserPostRequestValidator next) {
        this.next = next;
    }
    
    public boolean checkNext(UserPostRequest user) throws AlreadyExistsException, InvalidUsernameException, MalformedBodyException, InvalidEmailException {
        if(next == null)
            return true;
        return next.handle(user);
    }
    
    public void link(UserPostRequestValidator... others) {
        UserPostRequestValidator list = next;
        for(UserPostRequestValidator v : others) {
            list.setNextOperation(v);
            list = v;
        }
    }
}
