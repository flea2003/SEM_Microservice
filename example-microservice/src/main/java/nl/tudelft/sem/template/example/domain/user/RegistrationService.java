package nl.tudelft.sem.template.example.domain.user;

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

        User toSave = new User(username,email,password);
        if(!toSave.isValid())
            throw new InvalidUserException();
        else
            return userRepository.save(toSave);
    }
}
