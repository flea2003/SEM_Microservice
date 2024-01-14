package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;

public interface Validator <T>{
    void setNextOperation(Validator <T> handler);
    void link(Validator<T>... others);
    boolean handle(T request) throws InvalidUsernameException, MalformedBodyException, AlreadyExistsException, InvalidEmailException;
}
