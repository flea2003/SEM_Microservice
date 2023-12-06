package nl.tudelft.sem.template.example.domain.user;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * A DDD service for registering a new user.
 */
@Service
public class RegistrationService {
    private final transient UserRepository userRepository;

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository  the user repository
     */
    public RegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user.
     *
     * @param username The username for the new user
     * @param email The email for the new user
     * @param password The password for the new user
     */
    public User registerUser(String username, String email, String password) throws Exception {

        User toSave = new User(username, email, password);
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
        if (user.isEmpty()) {
            return null;
        }
        return user.get(0);
    }
}
