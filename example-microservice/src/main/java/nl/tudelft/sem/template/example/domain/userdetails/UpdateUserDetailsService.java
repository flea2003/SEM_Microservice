package nl.tudelft.sem.template.example.domain.userdetails;

import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import org.springframework.stereotype.Service;

/**
 * A DDD service for updating the user details.
 */
@Service
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
public class UpdateUserDetailsService {
    private final transient UserRepository userRepository;
    private final transient UserDetailsRepository userDetailsRepository;

    public UpdateUserDetailsService(UserRepository userRepository, UserDetailsRepository userDetailsRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    /**
     * Utility function for the checking below.
     *
     * @param field A list of objects to go through and check for nulls
     * @return whether the list contains nulls or not
     */
    public boolean arrayHasNullsUtility(List<?> field) {
        for (Object o : field) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * The new user details are validated here before updating.
     *
     * @param userDetails the new user details to replace with
     * @return whether the new user details are valid or not
     */

    public boolean checkValidUserDetails(UserDetails userDetails) {
        return userDetails.getId() != null
                && userDetails.getName() != null
                && userDetails.getBio() != null
                && userDetails.getLocation() != null
                && userDetails.getFollowing() != null
                && userDetails.getFavouriteBookId() != null
                && userDetails.getFavouriteGenres() != null
                && !arrayHasNullsUtility(userDetails.getFollowing())
                && !arrayHasNullsUtility(userDetails.getFavouriteGenres());
    }

    /**
     * Service method for updating the user details.
     *
     * @param userId the ID of the user to update the details of
     * @param userDetails the new details to replace with
     * @return the new updated user details
     * @throws InvalidUserDetailsException if the new given user details are invalid
     */
    public UserDetails updateUserDetails(Integer userId, UserDetails userDetails) throws InvalidUserDetailsException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return null;
        }
        UserDetails currentUserDetails = userOptional.get().getUserDetails();
        if (!checkValidUserDetails(userDetails)) {
            throw new InvalidUserDetailsException("New user details data is invalid");
        }
        currentUserDetails.editUserDetails(userDetails);
        return userDetailsRepository.save(currentUserDetails);
    }
}
