package nl.tudelft.sem.template.example.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettingsRepository;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.models.LoginPostRequest;
import nl.tudelft.sem.template.example.models.UserPostRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class userUserIDDelete {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private AccountSettingsRepository accountSettingsRepository;

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
        userDetailsRepository.deleteAll();
        accountSettingsRepository.deleteAll();
    }

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        userDetailsRepository.deleteAll();
        accountSettingsRepository.deleteAll();
    }

    @Test
    public void testDeleteValid() throws Exception{

        //Register the user
        UserPostRequest userPostRequest = new UserPostRequest("loginValid", "loginEmailValid@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostRequest.toJsonString()));

        // Assert
        result.andExpect(status().isOk());

        //Log in
        LoginPostRequest loginPostRequest = new LoginPostRequest("loginValid", "password");
        result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostRequest.toJsonString()));

        result.andExpect(status().isOk());

        String userString = result.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(userString);
        Integer id = jsonNode.get("id").asInt();

        // delete the user
        ResultActions resultDelete = mockMvc.perform(delete("/user/{userID}", id));
        resultDelete.andExpect(status().isOk());

        // check whether the user does not longer exist
        result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostRequest.toJsonString()));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteNoSuchUser() throws Exception{

        Integer id = -2;

        ResultActions resultDelete = mockMvc.perform(delete("/user/{userID}", id));
        resultDelete.andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteInvalidPath() throws Exception{
        ResultActions resultDelete = mockMvc.perform(delete("/user/{userID}", "stringInsteadOfInteger"));
        resultDelete.andExpect(status().isBadRequest());
    }




}
