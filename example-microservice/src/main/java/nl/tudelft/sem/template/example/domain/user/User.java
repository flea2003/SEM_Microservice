package nl.tudelft.sem.template.example.domain.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

/**
 * The user entity in our domain.
 */
@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false)
    @Convert(converter = UsernameConverter.class)
    private Username username;

    @Column(name = "email", nullable = false, unique=true)
    @Convert(converter = EmailConverter.class)
    private Email email;

    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    @Column(name = "userDetailsID")
    private Integer userDetailsID;

    @Column(name = "accountSettingsID")
    private Integer accountSettingsID;

    @Column(name = "isAdmin")
    private Boolean isAdmin;

    @Column(name = "isAuthor")
    private Boolean isAuthor;

    /**
     * Create new  user.
     *
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user
     */
    public User(String username, String email, String password) {
        this.username = new Username(username);
        this.email = new Email(email);
        this.password = PasswordHashingService.hash(password);
        this.isAdmin = false;
        this.isAuthor = false;
    }

    public boolean isValid(){
        //The username or the email was invalid => this is also invalid
        return this.username.toString() != null && this.email.toString() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username,email,password);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class User {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    email: ").append(toIndentedString(email)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    userDetailsID: ").append(toIndentedString(userDetailsID)).append("\n");
        sb.append("    accountSettingsID: ").append(toIndentedString(accountSettingsID)).append("\n");
        sb.append("    isAdmin: ").append(toIndentedString(isAdmin)).append("\n");
        sb.append("    isAuthor: ").append(toIndentedString(isAuthor)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
