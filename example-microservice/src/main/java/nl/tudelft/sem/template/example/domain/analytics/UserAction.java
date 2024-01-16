package nl.tudelft.sem.template.example.domain.analytics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.sem.template.example.domain.user.User;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "userActions")
@NoArgsConstructor
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "userID", referencedColumnName = "id")
    private User user;

    @Column(name = "type", nullable = false)
    private String type;

    /**
     * Creates a new user action given a user reference and an action type
     *
     * @param user the user that did this action
     * @param type the type of action that the user did
     */
    public UserAction(User user, String type) {
        assert user != null : "User cannot be null";
        assert type != null : "Type cannot be null";
        this.user = user;
        this.type = type;
    }

    @Override
    public boolean equals(Object other) {
        if(other == this)
            return true;
        if(other.getClass() != this.getClass())
            return false;

        UserAction action = (UserAction) other;
        return this.id == action.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(51 /* magic number */, this.id);
    }

    @Override
    public String toString() {
        return "User Action:\n" +
                "\tid = " + this.id + "\n" +
                "\ttype = " + this.type
                + "\nReferenced User:\n" + this.user.toString();
    }

}
