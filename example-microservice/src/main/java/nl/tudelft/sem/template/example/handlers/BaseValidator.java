package nl.tudelft.sem.template.example.handlers;

import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public abstract class BaseValidator<T> implements Validator<T> {
    private transient Validator<T> next;

    @Override
    public void setNextOperation(Validator<T> next) {
        this.next = next;
    }

    /**
     * Sets the next request to check.
     *
     * @param request request to check
     * @return The result of the check
     * @throws InputFormatException If the input is not formatted as expected
     */
    public boolean checkNext(T request) throws InputFormatException {
        if (next == null) {
            return true;
        }
        return next.handle(request);
    }

    /**
     * Links the validators together.
     *
     * @param others Other validators
     */
    public void link(Validator<T>... others) {
        if (others.length == 0) {
            return;
        }
        this.next = others[0];
        Validator<T> list = this.next;
        for (int i = 1; i < others.length; i++) {
            var v = others[i];
            list.setNextOperation(v);
            list = v;
        }
    }
}
