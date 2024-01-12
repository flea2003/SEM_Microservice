package nl.tudelft.sem.template.example.handlers;

import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
import nl.tudelft.sem.template.example.domain.user.User;

public interface Validator {
    void setNextOperation(Validator nextOperation);
    boolean handleUserOperation(User user);
    boolean handleDetailsOperation(UserDetails userDetails);
    boolean handleOperation(Validator operation);
}
