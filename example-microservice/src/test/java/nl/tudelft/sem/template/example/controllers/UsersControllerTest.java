package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.user.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.RegistrationService;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.models.UserPostRequest;
import nl.tudelft.sem.template.example.profiles.TestUserRepository;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static RegistrationService registrationService;
    @BeforeAll
    static void setup() throws Exception {
        registrationService = Mockito.mock(RegistrationService.class);

        //Invalid input registration
        when(registrationService.registerUser("!user","email@google.com","pass123")).thenThrow(new InvalidUserException());
    }
    @Test
    void registerEmptyInput() throws Exception{
        UserPostRequest userToAdd = new UserPostRequest("!user","email@google.com","");

        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON).requestAttr("userPostRequest", userToAdd));

        // Assert
        result.andExpect(status().isBadRequest()).andExpect(content().string("Request body is malformed"));
    }
}