package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.profiles.TestUserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationServiceTest {

    private static UserRepository userRepository;
    @BeforeAll
    static void setup(){
        userRepository = new TestUserRepository();
    }
    @Test
    void registerUser() throws Exception {
        UserRegistrationService sut = new UserRegistrationService(userRepository);
        User validUser1 = new User("name","email@google.com","pass123");
        validUser1.setId(1);
        User validUser2 = new User("name2","email2@google.com","pass123");
        validUser2.setId(2);
        //Register a valid user
        User result = sut.registerUser("name", "email@google.com", "pass123");
        assertEquals(validUser1,result);
        assertEquals(validUser1.toString(),result.toString());

        //Register an invalid one => exception
        assertThrows(InvalidUserException.class, ()->sut.registerUser("_a","ab@c.de","pass"));

        //Registering a valid user still works afterwards
        result = sut.registerUser("name2", "email2@google.com", "pass123");
        assertEquals(validUser2.getId(),result.getId());
        assertEquals(validUser2.toString(),result.toString());
    }

    @Test
    void getUserById() throws Exception {
        UserRegistrationService sut = new UserRegistrationService(userRepository);
        User validUser1 = new User("name","email@google.com","pass123");
        validUser1.setId(1);
        User validUser2 = new User("name2","email2@google.com","pass123");
        validUser2.setId(2);
        //Register a valid user
        sut.registerUser("name", "email@google.com", "pass123");
        sut.registerUser("name", "email2@google.com", "pass123");

        assertEquals(validUser2,sut.getUserById(2));
    }

    @Test
    void getUserByEmail() throws Exception{
        UserRegistrationService sut = new UserRegistrationService(userRepository);
        User validUser1 = new User("name","email@google.com","pass123");
        validUser1.setId(1);
        User validUser2 = new User("name2","email2@google.com","pass123");
        validUser2.setId(2);
        //Register a valid user
        sut.registerUser("name", "email@google.com", "pass123");
        sut.registerUser("name", "email2@google.com", "pass123");

        assertEquals(validUser2,sut.getUserByEmail("email2@google.com"));
        assertNull(sut.getUserByEmail("email3@google.com"));
    }
}