package nl.tudelft.sem.template.example.domain.UserDetails;

import java.util.Optional;
import java.util.stream.Collectors;

import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import org.springframework.stereotype.Service;

/**
 * A DDD service for registering a new userDetails.
 */
@Service
public class UserDetailsRegistrationService {
    private final transient UserDetailsRepository userDetailsRepository;

    /**
     * Instantiates a new UserDetailsService.
     *
     * @param userDetailsRepository  the userDetails repository
     */
    public UserDetailsRegistrationService(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    /**
     * Register a new userDetails.
     *
     */
    public UserDetails registerUserDetails() throws InvalidUserException {
        UserDetails detailsOfNewUser = new UserDetails();
        try {
            return userDetailsRepository.save(detailsOfNewUser);
        }catch (Exception e){
            throw new InvalidUserException("Couldn't register user");
        }
    }

    /**
     * Returns a UserDetails object by ID.
     * @param anyID the ID of the object to search
     * @return an optional of the object from the repository
     */
    public Optional<UserDetails> findById(Integer anyID) {
        return userDetailsRepository.findById(anyID);
    }

}
