package nl.tudelft.sem.template.example.handlers;

import nl.tudelft.sem.template.example.domain.exceptions.*;

public abstract class BaseValidator<T> implements Validator<T> {
    private Validator<T> next;

    @Override
    public void setNextOperation(Validator<T> next) {
        this.next = next;
    }
    
    public boolean checkNext(T request) throws InputFormatException {
        if(next == null)
            return true;
        return next.handle(request);
    }
    
    public void link(Validator<T>... others) {
        if(others.length == 0) {
            return;
        }
        this.next = others[0];
        Validator<T> list = this.next;
        for(int i = 1;i < others.length;i++) {
            var v = others[i];
            list.setNextOperation(v);
            list = v;
        }
    }
}
