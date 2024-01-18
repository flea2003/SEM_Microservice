package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.domain.userdetails.UserDetails;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

class UserDetailsRegistrationServiceTest {

    private static UserDetailsRepository userDetailsRepository;
    @BeforeAll
    static void setup(){
        userDetailsRepository = Mockito.mock(UserDetailsRepository.class);
    }

    @Test
    void registerUserSuccesful() throws Exception {
        UserDetailsRegistrationService sut = new UserDetailsRegistrationService(userDetailsRepository);
        UserDetails userDetailsToReturn = new UserDetails(1, "Yoda", "Jedi I am",
                "Dagobah", "", null, -1, null);
        when(sut.registerUserDetails()).thenReturn(userDetailsToReturn);
        assertEquals(sut.registerUserDetails(), userDetailsToReturn);
    }

    @Test
    void registerUserUnsuccesful() throws Exception {
        UserDetailsRegistrationService sut = new UserDetailsRegistrationService(userDetailsRepository);
        UserDetails userDetailsToReturn = new UserDetails(1, "Yoda", "Jedi I am",
                "Dagobah", "", null, -1, null);
        given(sut.registerUserDetails()).willAnswer( invocation -> { throw new InvalidUserException(); });
        assertThrows(InvalidUserException.class, sut::registerUserDetails);
    }

}