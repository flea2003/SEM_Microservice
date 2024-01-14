//package nl.tudelft.sem.template.example.handlers.string;
//
//import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
//import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
//import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
//import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
//import nl.tudelft.sem.template.example.models.LoginPostRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.NoSuchElementException;
//
//public class LoginValidator <T extends LoginPostRequest> extends BaseValidator<T>{
//
//    @Override
//    public boolean handle(T login) throws InvalidUsernameException, AlreadyExistsException, MalformedBodyException, InvalidEmailException {
//        String username = login.getUsername();
//        String password = login.getPassword();
//
//        if (username == null || username.isEmpty()
//                || password == null || password.isEmpty()) {
//            throw new
//        }
//        return super.checkNext(login);
//    }
//}
