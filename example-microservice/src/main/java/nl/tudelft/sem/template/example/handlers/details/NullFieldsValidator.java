package nl.tudelft.sem.template.example.handlers.details;

import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.handlers.BaseValidator;

public class NullFieldsValidator<T extends EditUserRequestParameters> extends BaseValidator<T> {
    @Override
    public boolean handle(T request) throws InputFormatException {
        if (request.getUserId() == null || request.getUserDetails() == null) {
            throw new MalformedBodyException("Request body is malformed");
        }
        return super.checkNext(request);
    }
}
