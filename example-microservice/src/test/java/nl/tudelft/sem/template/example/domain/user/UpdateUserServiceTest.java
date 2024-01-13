package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.profiles.TestUserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

    @Test
    void testNotFound(){
        UserRepository ur2 = Mockito.mock(UserRepository.class);
        when(ur2.findById(10)).thenReturn(Optional.empty());
        UpdateUserService sut2 = new UpdateUserService(ur2);

        assertNull(sut2.changePassword(10,PasswordHashingService.hash("pass")));
    }

    @Test
    void userModifiedTest(){
        UserRepository ur2 = Mockito.mock(UserRepository.class);
        User userNew = new User();
        userNew.setId(5);
        userNew.setPassword(PasswordHashingService.hash("passOld"));
        when(ur2.findById(5)).thenReturn(Optional.of(userNew));
        UpdateUserService sut2 = new UpdateUserService(ur2);

        sut2.changePassword(5,PasswordHashingService.hash("passNew"));
        assertEquals(PasswordHashingService.hash("passNew").toString(),userNew.getPassword().toString());
    }
}
