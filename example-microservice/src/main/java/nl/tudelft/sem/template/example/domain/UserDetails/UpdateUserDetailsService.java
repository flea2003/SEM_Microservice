package nl.tudelft.sem.template.example.domain.UserDetails;

import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A DDD service for updating the user details.
 */
@Service
public class UpdateUserDetailsService {
    private final transient UserRepository userRepository;
    private final transient UserDetailsRepository userDetailsRepository;

    public UpdateUserDetailsService(UserRepository userRepository, UserDetailsRepository userDetailsRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    public boolean checkValidUserDetails(UserDetails userDetails) {
        return !userDetails.getName().getValue().equals("");
    }
    public UserDetails updateUserDetails(Integer userID, UserDetails userDetails) throws InvalidUserDetailsException {
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isPresent()) {
            UserDetails currentUserDetails = userOptional.get().getUserDetails();
            if (!checkValidUserDetails(userDetails)) {
                throw new InvalidUserDetailsException("New user details data is invalid");
            }
            currentUserDetails.editUserDetails(userDetails);
            return userDetailsRepository.save(currentUserDetails);
            // no need to also update it in the User repository since they are linked by id
            // editUserDetails function does not modify id
        }
        return null;
    }
}
