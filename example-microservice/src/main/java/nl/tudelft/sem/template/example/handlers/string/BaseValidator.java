package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.models.UserPostRequest;

import javax.validation.Valid;

public abstract class BaseValidator<T> implements Validator<T> {
    private Validator<T> next;

    @Override
    public void setNextOperation(Validator<T> next) {
        this.next = next;
    }
    
    public boolean checkNext(T request) throws AlreadyExistsException, InvalidUsernameException, MalformedBodyException, InvalidEmailException {
        if(next == null)
            return true;
        return next.handle(request);
    }
    
    public void link(Validator<T>... others) {
        Validator<T> list = next;
        for(var v : others) {
            list.setNextOperation(v);
            list = v;
        }
    }
}
