package nl.tudelft.sem.template.example.domain.UserDetails;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.example.domain.user.EmailConverter;
import nl.tudelft.sem.template.example.domain.user.NameConverter;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UsernameConverter;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * UserDetails
 */

@Getter
@Setter
@Entity
@Table(name = "usersDetails")
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonProperty("name")
    @Convert(converter = NameConverter.class)
    @Column(name = "name", nullable = false, unique=false)
    private Name name;

    @Column(name = "bio", nullable = false, unique=false)
    private String bio;


    @Column(name = "location", nullable = false, unique=false)
    private String location;

    @Column(name = "profilePicture", nullable = true, unique=false)
    private String profilePicture;


    @ElementCollection
    @CollectionTable(name = "user_following_each_other", joinColumns = @JoinColumn(name = "idUserDetails"))
    @Column(name = "user_followed")
    private List<User> following;

    @Column(name = "favoriteBookID", nullable = true, unique=false)
    private Integer favouriteBookID;

    @ElementCollection
    @CollectionTable(name = "user_favorite_genres", joinColumns = @JoinColumn(name = "user_details_id"))
    @Column(name = "favorite_genre")
    private List<String> favouriteGenres;

    /**
     * Default constructor
     */
    public UserDetails(){
        this.name = new Name("");
        this.bio = "";
        this.location = "";
        this.profilePicture = "";
        this.following = new ArrayList<>();
        this.favouriteBookID = -1;
        this.favouriteGenres = new ArrayList<>();
    }

    /**
     * Creates a new UserDetails
     * @param id - the id of the corresponding user details
     * @param name - the full name of the user
     * @param bio - the bio description of the user
     * @param location - the location of the user
     * @param profilePicture - the profile picture of the user encoded as a base64 string
     * @param following - the list of users that the corresponding users follows
     * @param favouriteBookID  - the id of the user's corresponding favorite book
     * @param favouriteGenres - the list of users favorites genres
     */
    public UserDetails(Integer id, String name, String bio, String location, String profilePicture, List<User> following, Integer favouriteBookID, List<String>favouriteGenres) {
        this.id = id;
        this.name = new Name(name);
        this.bio = bio;
        this.location = location;
        this.profilePicture = profilePicture;
        this.following = following;
        this.favouriteBookID = favouriteBookID;
        this.favouriteGenres = favouriteGenres;
    }

    /**
     * Constructor which takes only the id as a parameter
     * @param id
     */
    public UserDetails(Integer id) {
        this.id = id;
        this.name = new Name("");
        this.bio = "";
        this.location = "";
        this.profilePicture = "";
        this.following = new ArrayList<>();
        this.favouriteBookID = -1;
        this.favouriteGenres = new ArrayList<>();
    }


    public UserDetails addFollowingItem(User followingItem) {
        if (this.following == null) {
            this.following = new ArrayList<>();
        }
        this.following.add(followingItem);
        return this;
    }


    public UserDetails addFavouriteGenresItem(String favouriteGenresItem) {
        if (this.favouriteGenres == null) {
            this.favouriteGenres = new ArrayList<>();
        }
        this.favouriteGenres.add(favouriteGenresItem);
        return this;
    }

    public void editUserDetails(UserDetails userDetails) {
        this.name = userDetails.getName();
        this.bio = userDetails.getBio();
        this.favouriteBookID = userDetails.getFavouriteBookID();
        this.location = userDetails.getLocation();
        this.profilePicture = userDetails.getProfilePicture();
        this.following = userDetails.getFollowing();
        this.favouriteGenres = userDetails.getFavouriteGenres();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDetails userDetails = (UserDetails) o;
        return Objects.equals(this.id, userDetails.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, bio, location, profilePicture, favouriteBookID, favouriteGenres);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserDetails {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    bio: ").append(toIndentedString(bio)).append("\n");
        sb.append("    location: ").append(toIndentedString(location)).append("\n");
        sb.append("    profilePicture: ").append(toIndentedString(profilePicture)).append("\n");
        sb.append("    following: ").append(toIndentedString(following)).append("\n");
        sb.append("    favouriteBookID: ").append(toIndentedString(favouriteBookID)).append("\n");
        sb.append("    favouriteGenres: ").append(toIndentedString(favouriteGenres)).append("\n");
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

