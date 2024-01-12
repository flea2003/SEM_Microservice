package nl.tudelft.sem.template.example.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettingsRepository;
import nl.tudelft.sem.template.example.domain.AccountSettings.NOTIFICATIONS;
import nl.tudelft.sem.template.example.domain.AccountSettings.PRIVACY;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class userUserIDUpdateAccountSettingsPut {


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
    public void testUpdateAccountSettingsValid() throws Exception{

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

        // update the account settings
        AccountSettings accountSettings = new AccountSettings(jsonNode.get("accountSettings").asInt(), PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        String json = objectMapper.writeValueAsString(accountSettings);

        ResultActions resultUpdate = mockMvc.perform(put("/user/{userID}/updateAccountSettings", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
        resultUpdate.andExpect(status().isOk());
    }


    @Test
    public void testUpdateNonLoggedHackerTries() throws Exception{

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

        // update the account settings
        AccountSettings accountSettings = new AccountSettings(jsonNode.get("accountSettings").asInt() + 1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        String json = objectMapper.writeValueAsString(accountSettings);

        ResultActions resultUpdate = mockMvc.perform(put("/user/{userID}/updateAccountSettings", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
        resultUpdate.andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateBadRequest1() throws Exception{

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

        // update the account settings
        AccountSettings accountSettings = new AccountSettings(jsonNode.get("accountSettings").asInt() + 1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        String json = objectMapper.writeValueAsString(accountSettings);

        ResultActions resultUpdate = mockMvc.perform(put("/user/{userID}/updateAccountSettings", "lol")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
        resultUpdate.andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateBadRequest2() throws Exception{

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

        // update the account settings
        String json = "random string";

        ResultActions resultUpdate = mockMvc.perform(put("/user/{userID}/updateAccountSettings", "lol")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
        resultUpdate.andExpect(status().isBadRequest());
    }



}

