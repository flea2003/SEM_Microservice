package nl.tudelft.sem.template.example.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class userUserIDDelete {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDeleteValid() throws Exception{

        UserPostRequest userPostRequest = new UserPostRequest("1register", "registerEmailIncorrectFormat@gmail.com", "password");
        ResultActions resultCreate = mockMvc.perform(MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostRequest.toJsonString()));

        resultCreate.andExpect(status().isOk());
        String response = resultCreate.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("User created successfully");

    }

    @Test
    public void testDeleteNoSuchUser() throws Exception{

        UserPostRequest userPostRequest = new UserPostRequest("1register", "registerEmailIncorrectFormat@gmail.com", "password");
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostRequest.toJsonString()));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("User created successfully");

    }


//    @Test
//    public void testDeleteValid(){
//
//        UserPostRequest userPostRequest = new UserPostRequest("1register", "registerEmailIncorrectFormat@gmail.com", "password");
//        ResultActions result = mockMvc.perform(post("/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(userPostToString(userPostRequest)));
//
//        // Assert
//        result.andExpect(status().isOk());
//
//        String response = result.andReturn().getResponse().getContentAsString();
//
//        assertThat(response).isEqualTo("User created successfully");
//
//    }
//
//    @Test
//    public void testDeleteValid(){
//
//        UserPostRequest userPostRequest = new UserPostRequest("1register", "registerEmailIncorrectFormat@gmail.com", "password");
//        ResultActions result = mockMvc.perform(post("/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(userPostToString(userPostRequest)));
//
//        // Assert
//        result.andExpect(status().isOk());
//
//        String response = result.andReturn().getResponse().getContentAsString();
//
//        assertThat(response).isEqualTo("User created successfully");
//
//    }
}
