package nl.tudelft.sem.template.example.handlers;

import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;

public interface Validator<T> {
    void setNextOperation(Validator<T> handler);

    void link(Validator<T>... others);

    boolean handle(T request) throws InputFormatException;
}
