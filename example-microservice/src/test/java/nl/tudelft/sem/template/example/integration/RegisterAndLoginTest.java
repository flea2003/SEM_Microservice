package nl.tudelft.sem.template.example.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.tudelft.sem.template.example.models.LoginPostRequest;
import nl.tudelft.sem.template.example.models.UserPostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class RegisterAndLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRegisterValid() throws Exception {
        // Arrange

        // Act
        UserPostRequest userPostRequest = new UserPostRequest("registerValid", "registerEmailValid@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User created successfully");

    }

    @Test
    public void testRegisterIncorrectFormat() throws Exception {
        // Arrange

        // Act
        UserPostRequest userPostRequest = new UserPostRequest("1register", "registerEmailIncorrectFormat@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isBadRequest());

        userPostRequest = new UserPostRequest("registerIncorrectFormat", "registerEmail", "password");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isBadRequest());

    }

    @Test
    public void testDuplicateEmail() throws Exception {
        // Arrange

        // Act
        UserPostRequest userPostRequest = new UserPostRequest("registerDup", "registerEmailDup@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        userPostRequest = new UserPostRequest("registerDup2", "registerEmailDup@gmail.com", "password2");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isConflict());

    }

    @Test
    public void testLoginValid() throws Exception {
        //Register the user
        UserPostRequest userPostRequest = new UserPostRequest("loginValid", "loginEmailValid@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        //Log in
        LoginPostRequest loginPostRequest = new LoginPostRequest("loginValid", "password");
        result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPostToString(loginPostRequest)));

        result.andExpect(status().isOk());

        String userString = result.andReturn().getResponse().getContentAsString();

        //Assert correct user
        assertTrue(userString.contains("\"username\":\"loginValid\""));
        assertTrue(userString.contains("\"email\":\"loginEmailValid@gmail.com\""));
    }

    @Test
    public void testLoginBadUsername() throws Exception {
        //Register the user
        UserPostRequest userPostRequest = new UserPostRequest("loginBadUsername", "loginEmailBadUsername@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        //Log in
        LoginPostRequest loginPostRequest = new LoginPostRequest("loginWrong", "password");
        result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPostToString(loginPostRequest)));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void testLoginBadPassword() throws Exception {
        //Register the user
        UserPostRequest userPostRequest = new UserPostRequest("loginBadPassword", "loginEmailBadPassword@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        //Log in
        LoginPostRequest loginPostRequest = new LoginPostRequest("loginBadPassword", "password2");
        result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPostToString(loginPostRequest)));

        result.andExpect(status().isUnauthorized());
    }

    private String userPostToString(UserPostRequest userPost){
        String sb = "{\n" +
                "    \"username\": " + "\"" + userPost.getUsername() + "\"," + "\n" +
                "    \"email\": " + "\"" + userPost.getEmail() + "\"," + "\n" +
                "    \"password\": " + "\"" + userPost.getPassword() + "\"" + "\n" +
                "}";
        return sb;
    }

    private String loginPostToString(LoginPostRequest login){
        String sb = "{\n" +
                "    \"username\": " + "\"" + login.getUsername() + "\"," + "\n" +
                "    \"password\": " + "\"" + login.getPassword() + "\"" + "\n" +
                "}";
        return sb;
    }
}
