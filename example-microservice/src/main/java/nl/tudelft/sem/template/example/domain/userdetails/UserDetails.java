package nl.tudelft.sem.template.example.domain.userdetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.example.domain.user.User;

/**
 * UserDetails.
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
    @Column(name = "name", nullable = false, unique = false)
    private Name name;

    @Column(name = "bio", nullable = false, unique = false)
    private String bio;


    @Column(name = "location", nullable = false, unique = false)
    private String location;

    @Column(name = "profilePicture", nullable = true, unique = false)
    private String profilePicture;


    @ElementCollection
    @CollectionTable(name = "user_following_each_other", joinColumns = @JoinColumn(name = "idUserDetails"))
    @Column(name = "user_followed")
    private List<User> following;

    @Column(name = "favoriteBookID", nullable = true, unique = false)
    private Integer favouriteBookId;

    @ElementCollection
    @CollectionTable(name = "user_favorite_genres", joinColumns = @JoinColumn(name = "user_details_id"))
    @Column(name = "favorite_genre")
    private List<String> favouriteGenres;

    /**
     * Default constructor.
     */
    public UserDetails() {
        this.name = new Name("");
        this.bio = "";
        this.location = "";
        this.profilePicture = "";
        this.following = new ArrayList<>();
        this.favouriteBookId = -1;
        this.favouriteGenres = new ArrayList<>();
    }

    /**
     * Creates a new UserDetails.
     *
     * @param id - the id of the corresponding user details
     * @param name - the full name of the user
     * @param bio - the bio description of the user
     * @param location - the location of the user
     * @param profilePicture - the profile picture of the user encoded as a base64 string
     * @param following - the list of users that the corresponding users follows
     * @param favouriteBookId  - the id of the user's corresponding favorite book
     * @param favouriteGenres - the list of users favorites genres
     */
    public UserDetails(Integer id, String name, String bio, String location, String profilePicture,
                       List<User> following, Integer favouriteBookId, List<String> favouriteGenres) {
        this.id = id;
        this.name = new Name(name);
        this.bio = bio;
        this.location = location;
        this.profilePicture = profilePicture;
        if (this.following == null) {
            this.following = new ArrayList<>();
        } else {
            this.following = following;
        }
        this.favouriteBookId = favouriteBookId;
        this.favouriteGenres = favouriteGenres;
    }

    /**
     * Constructor which takes only the id as a parameter.
     *
     * @param id id
     */
    public UserDetails(Integer id) {
        this.id = id;
        this.name = new Name("");
        this.bio = "";
        this.location = "";
        this.profilePicture = "";
        this.following = new ArrayList<>();
        this.favouriteBookId = -1;
        this.favouriteGenres = new ArrayList<>();
    }


    public UserDetails addFollowingItem(User followingItem) {
        this.following.add(followingItem);
        return this;
    }

    public UserDetails removeFollowingItem(User followingItem) {
        this.following.remove(followingItem);
        return this;
    }

    /**
     * Add favorite genres.
     *
     * @param favouriteGenresItem genre
     * @return this
     */
    public UserDetails addFavouriteGenresItem(String favouriteGenresItem) {
        if (this.favouriteGenres == null) {
            this.favouriteGenres = new ArrayList<>();
        }
        this.favouriteGenres.add(favouriteGenresItem);
        return this;
    }

    /**
     * Edit current user details.
     *
     * @param userDetails new details
     */
    public void editUserDetails(UserDetails userDetails) {
        this.name = userDetails.getName();
        this.bio = userDetails.getBio();
        this.favouriteBookId = userDetails.getFavouriteBookId();
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
        return Objects.equals(this.id, ((UserDetails) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, bio, location, profilePicture, favouriteBookId, favouriteGenres);
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
        sb.append("    following: ").append(toIndentedString(toSpecialString(following))).append("\n");
        sb.append("    favouriteBookID: ").append(toIndentedString(favouriteBookId)).append("\n");
        sb.append("    favouriteGenres: ").append(toIndentedString(favouriteGenres)).append("\n");
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

    /**
     * Special method to make following only display the user IDs.
     *
     * @param following List of Users followed
     * @return String representation of array
     */
    private String toSpecialString(List<User> following) {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < following.size(); i++) {
            result.append(following.get(i).getId());
            if (i != following.size() - 1) {
                result.append(",");
            }
        }
        return result.append("]").toString();
    }

    /**
     * Check whether a user is followed.
     *
     * @param other User to check for
     * @return true, iff user is followed
     */
    public Boolean isFollowed(User other) {
        Optional<User> toGet = following.stream()
                .filter(x -> x.getId().equals(other.getId()))
                .findFirst();
        return toGet.isPresent();
    }

}

