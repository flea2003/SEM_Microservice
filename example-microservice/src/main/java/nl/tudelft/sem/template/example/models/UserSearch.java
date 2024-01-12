package nl.tudelft.sem.template.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.example.domain.user.Email;

@Getter
@Setter
@EqualsAndHashCode
public class UserSearch {
    private String username;
    private Email email;

    public UserSearch(String username, Email email) {
        this.username = username;
        this.email = email;
    }

    public UserSearch() {
        this.username = "";
        this.email = null;
    }

    @JsonCreator
    public static UserSearch create(@JsonProperty("username") String username, @JsonProperty("email") String email) {
        return new UserSearch(username, new Email(email));
    }

}
