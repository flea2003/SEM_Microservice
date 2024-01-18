package nl.tudelft.sem.template.example.domain.user;

import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * A DDD service for updating the user.
 */
@Service
public class UpdateUserService {
    private final transient UserRepository userRepository;

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository  the user repository
     */
    public UpdateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Updates the password of the user.
     *
     * @param userId The id of the user.
     * @param password The password for the new user.
     */
    public User changePassword(int userId, HashedPassword password) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty() || password == null) {
            return null;
        }
        User user = optionalUser.get();
        user.setPassword(password);
        user.setIsAdmin(!user.getIsAdmin());
        userRepository.save(user);
        user.setIsAdmin(!user.getIsAdmin());
        return userRepository.save(user);
    }

}
