package nl.tudelft.sem.template.example.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import lombok.Setter;


/**
 * LoginPostRequest class.
 */
@Setter
@JsonTypeName("_login_post_request")
public class LoginPostRequest {
    private String username;
    private String password;

    /**
     * Constructor for the login request.
     *
     * @param username The username of the user
     * @param password The password of the user
     */
    public LoginPostRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Getter for the username.
     *
     * @return the username
     */
    @Schema(name = "username", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    /**
     * Getter for the password.
     *
     * @return the password
     */
    @Schema(name = "password", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LoginPostRequest userPostRequest = (LoginPostRequest) o;
        return Objects.equals(this.username, userPostRequest.username)
                && Objects.equals(this.password, userPostRequest.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LoginPostRequest {\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        return o == null
                ? "null"
                : o.toString().replace("\n", "\n    ");
    }
}

