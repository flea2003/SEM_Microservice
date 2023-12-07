package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.profiles.TestUserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserServiceTest {

    private static UserRepository userRepository;

    @BeforeAll
    static void setup() {
        userRepository = new TestUserRepository();
        User userEx = new User("Kevin", "kevin@google.com", "password");
        userEx.setId(1);
        userRepository.save(userEx);
    }

    @Test
    void changePassword(){
        UpdateUserService sut = new UpdateUserService(userRepository);

        //Change password
        HashedPassword hashed = PasswordHashingService.hash("pass123");
        User result = sut.changePassword(1, hashed);
        assertEquals(hashed, result.getPassword());
    }
}
