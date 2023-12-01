package nl.tudelft.sem.template.example.domain.user;

import lombok.EqualsAndHashCode;

import java.util.regex.Pattern;

@EqualsAndHashCode
public class Email {
    private final transient String email;

    public Email(String email) {
        if(!validate(email))
            this.email = null;
        else
            this.email = email;
    }

    private boolean validate(String email) {
        return Pattern.matches("^[a-zA-Z][a-zA-Z0-9-_]*@[a-zA-Z][a-zA-Z0-9-_]*.[a-zA-Z][a-zA-Z0-9-_]{1,3}",email);
    }

    @Override
    public String toString() {
        return email;
    }
}
