package nl.tudelft.sem.template.example.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * A DDD service for registering a new user.
 */
@Service
public class UserRegistrationService {
    private final transient UserRepository userRepository;

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository  the user repository
     */
    public UserRegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user.
     *
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user
     */
    public User registerUser(String username, String email, String password) throws InvalidUserException {
        User toSave = new User(username, email, password);

        if (!toSave.isValid()) {
            throw new InvalidUserException();
        } else {
            return userRepository.save(toSave);
        }
    }

    /**
     * Function to register a user.
     *
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user
     * @param userDetails The details for the user
     * @return The registered user
     * @throws Exception if user cannot be saved
     */
    public User registerUser(String username, String email, String password,
                             UserDetails userDetails, AccountSettings accountSettings) throws Exception {
        User toSave = registerUser(username, email, password);
        toSave.setUserDetails(userDetails);
        toSave.setAccountSettings(accountSettings);

        if (!toSave.isValid()) {
            throw new InvalidUserException();
        } else {
            return userRepository.save(toSave);
        }
    }


    /**
     * Get user by id.
     *
     * @param id User id of user to fetch
     * @return The user with the specified ID, or null if it does not exist in the database
     */
    public User getUserById(int id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    /**
     * Get user by email.
     *
     * @param email Email id of user to fetch
     * @return The user with the specified email, or null if it does not exist in the database
     */
    public User getUserByEmail(String email) {
        var user = userRepository.findAll()
                .stream()
                .filter(x -> x.getEmail().toString().equals(email))
                .collect(Collectors.toList());

        return !user.isEmpty()
                ? user.get(0)
                : null;
    }

    /**
     * Get user by username.
     *
     * @param username username of user to fetch
     * @return List of Users with the specified username, or null if it does not exist in the database
     */
    public List<User> getUserByUsername(String username) {
        var users = userRepository.findAll()
                .stream()
                .filter(x -> x.getUsername().toString().equals(username))
                .collect(Collectors.toList());

        return !users.isEmpty()
                ? users
                : null;
    }
}
