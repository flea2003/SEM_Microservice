package nl.tudelft.sem.template.example.integration;

import nl.tudelft.sem.template.example.models.UserPostRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testBanNotFound() throws Exception{
        //Try to ban the user
        ResultActions result = mockMvc.perform(put("/admin/" + 12234 + "/banUser/" + 56678));

        // Assert
        result.andExpect(status().isNotFound());
    }
    @Test
    public void testBanUserNoPrivileges() throws Exception{
        //Register the user to perform the request
        UserPostRequest userPostRequest = new UserPostRequest("banNotAdmin", "banNotAdmin@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfPerformingUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Register the user to ban
        userPostRequest = new UserPostRequest("banNotAdminUser", "banNotAdminUser@gmail.com", "password");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfBannedUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Try to ban the user
        result = mockMvc.perform(put("/admin/" + idOfPerformingUser + "/banUser/" + idOfBannedUser));

        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void testBanUserSuccess() throws Exception{
        //Register the user to perform the request
        UserPostRequest userPostRequest = new UserPostRequest("banAdmin", "banAdmin@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfPerformingUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Register the user to ban
        userPostRequest = new UserPostRequest("banAdminUser", "banAdminUser@gmail.com", "password");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfBannedUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Make User 1 admin
        result = mockMvc.perform(post("/user/" + idOfPerformingUser + "/makeAdmin")
                .contentType(MediaType.TEXT_PLAIN)
                .content("bookManiaAdminPassword@Admin"));

        // Assert
        result.andExpect(status().isOk());

        //Try to ban the user
        result = mockMvc.perform(put("/admin/" + idOfPerformingUser + "/banUser/" + idOfBannedUser));

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testUnbanNotFound() throws Exception{
        //Try to unban the user
        ResultActions result = mockMvc.perform(put("/admin/" + 12234 + "/unbanUser/" + 56678));

        // Assert
        result.andExpect(status().isNotFound());
    }
    @Test
    public void testUnbanUserNoPrivileges() throws Exception{
        //Register the user to perform the request
        UserPostRequest userPostRequest = new UserPostRequest("unbanNotAdmin", "unbanNotAdmin@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfPerformingUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Register the user to unban
        userPostRequest = new UserPostRequest("unbanNotAdminUser", "unbanNotAdminUser@gmail.com", "password");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfBannedUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Try to unban the user
        result = mockMvc.perform(put("/admin/" + idOfPerformingUser + "/unbanUser/" + idOfBannedUser));

        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void testUnbanUserSuccess() throws Exception{
        //Register the user to perform the request
        UserPostRequest userPostRequest = new UserPostRequest("unbanAdmin", "unbanAdmin@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfPerformingUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Register the user to unban
        userPostRequest = new UserPostRequest("unbanAdminUser", "unbanAdminUser@gmail.com", "password");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfBannedUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Make User 1 admin
        result = mockMvc.perform(post("/user/" + idOfPerformingUser + "/makeAdmin")
                .contentType(MediaType.TEXT_PLAIN)
                .content("bookManiaAdminPassword@Admin"));

        // Assert
        result.andExpect(status().isOk());

        //Try to unban the user
        result = mockMvc.perform(put("/admin/" + idOfPerformingUser + "/unbanUser/" + idOfBannedUser));

        // Assert
        result.andExpect(status().isOk());
    }

    @Test
    public void testDeleteNotFound() throws Exception{
        //Try to delete the user
        ResultActions result = mockMvc.perform(delete("/admin/" + 12234 + "/deleteUser/" + 56678));

        // Assert
        result.andExpect(status().isNotFound());
    }
    @Test
    public void testDeleteUserNoPrivileges() throws Exception{
        //Register the user to perform the request
        UserPostRequest userPostRequest = new UserPostRequest("deleteNotAdmin", "deleteNotAdmin@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfPerformingUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Register the user to delete
        userPostRequest = new UserPostRequest("deleteNotAdminUser", "deleteNotAdminUser@gmail.com", "password");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfDeletedUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Try to delete the user
        result = mockMvc.perform(delete("/admin/" + idOfPerformingUser + "/deleteUser/" + idOfDeletedUser));

        // Assert
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteUserSuccess() throws Exception{
        //Register the user to perform the request
        UserPostRequest userPostRequest = new UserPostRequest("deleteAdmin", "deleteAdmin@gmail.com", "password");
        ResultActions result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfPerformingUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Register the user to delete
        userPostRequest = new UserPostRequest("deleteAdminUser", "deleteAdminUser@gmail.com", "password");
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPostToString(userPostRequest)));

        // Assert
        result.andExpect(status().isOk());

        int idOfDeletedUser = Integer.parseInt(Objects.requireNonNull(result.andReturn().getResponse().getHeader("Logged in user ID")));

        //Make User 1 admin
        result = mockMvc.perform(post("/user/" + idOfPerformingUser + "/makeAdmin")
                .contentType(MediaType.TEXT_PLAIN)
                .content("bookManiaAdminPassword@Admin"));

        // Assert
        result.andExpect(status().isOk());

        //Try to delete the user
        result = mockMvc.perform(delete("/admin/" + idOfPerformingUser + "/deleteUser/" + idOfDeletedUser));

        // Assert
        result.andExpect(status().isOk());
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