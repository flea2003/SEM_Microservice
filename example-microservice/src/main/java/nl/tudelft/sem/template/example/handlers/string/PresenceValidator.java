package nl.tudelft.sem.template.example.handlers.string;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

public class PresenceValidator<T, G extends JpaRepository> extends BaseValidator<T>{

    private G repository;

    public PresenceValidator(G repository) {
        this.repository = repository;
    }

    @Override
    public boolean handle(T id) throws InvalidUsernameException, AlreadyExistsException, MalformedBodyException, InvalidEmailException {
        var optionalUser = repository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new NoSuchElementException();
        }
        return super.checkNext(id);
    }

}
