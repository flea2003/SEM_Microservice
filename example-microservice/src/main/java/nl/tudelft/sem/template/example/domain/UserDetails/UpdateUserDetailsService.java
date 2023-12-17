package nl.tudelft.sem.template.example.domain.UserDetails;

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

    public UserDetails updateUserDetails(Integer userID, UserDetails userDetails)
    {
        Optional<User> userOptional = userRepository.findById(userID);
        if (userOptional.isPresent()) {
            UserDetails currentUserDetails = userOptional.get().getUserDetails();
            currentUserDetails.editUserDetails(userDetails);
            return userDetailsRepository.save(currentUserDetails);
        }
        return null;
    }
}
