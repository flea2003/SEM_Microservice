package nl.tudelft.sem.template.example.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.tudelft.sem.template.example.models.UserPostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class RegisterTest {

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

    private String userPostToString(UserPostRequest userPost){
        String sb = "{\n" +
                "    \"username\": " + "\"" + userPost.getUsername() + "\"," + "\n" +
                "    \"email\": " + "\"" + userPost.getEmail() + "\"," + "\n" +
                "    \"password\": " + "\"" + userPost.getPassword() + "\"" + "\n" +
                "}";
        return sb;
    }
}
