package nl.tudelft.sem.template.example.domain.user;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.analytics.UserAction;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetails;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonProperty("username")
    @Column(name = "username", nullable = false)
    @Convert(converter = UsernameConverter.class)
    private Username username;

    @JsonProperty("email")
    @Column(name = "email", nullable = false, unique = true)
    @Convert(converter = EmailConverter.class)
    private Email email;

    @JsonProperty("password_hash")
    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    // I modified the id to UserDetails because otherwise spring won't create tables
    // the annotation manages the serialization and deserialization :D
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_details_id", referencedColumnName = "id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private UserDetails userDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_settings_id", referencedColumnName = "id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private AccountSettings accountSettings;


    @Column(name = "isAdmin")
    private Boolean isAdmin;

    @Column(name = "isAuthor")
    private Boolean isAuthor;

    @Column(name = "isBanned")
    private Boolean isBanned;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAction> actions = new ArrayList<>();

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
        this.isBanned = false;
    }

    /**
     * Create a new user, but also gives the corresponding userDetails.
     *
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user
     * @param userDetails The Details for the new user
     */
    public User(String username, String email, String password, UserDetails userDetails, AccountSettings accountSettings) {
        this.username = new Username(username);
        this.email = new Email(email);
        this.password = PasswordHashingService.hash(password);
        this.userDetails = userDetails;
        this.accountSettings = accountSettings;
        this.isAdmin = false;
        this.isAuthor = false;
        this.isBanned = false;
    }

    // if it was public, the field was automatically added in the json response
    protected boolean isValid() {
        //The username or the email was invalid => this is also invalid
        return this.username.toString() != null && this.email.toString() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(id, ((User) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, password);
    }

    @Override
    public String toString() {
        return "class User {\n"
                + "    id: " + toIndentedString(id) + "\n"
                + "    username: " + toIndentedString(username) + "\n"
                + "    email: " + toIndentedString(email) + "\n"
                + "    password: " + toIndentedString(password) + "\n"
                + "    userDetails: " + toIndentedString(userDetails) + "\n"
                + "    accountSettings: " + toIndentedString(accountSettings) + "\n"
                + "    isAdmin: " + toIndentedString(isAdmin) + "\n"
                + "    isAuthor: " + toIndentedString(isAuthor) + "\n"
                + "    isBanned: " + toIndentedString(isBanned) + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        return o == null ? "null"
            : o.toString().replace("\n", "\n    ");
    }
}
