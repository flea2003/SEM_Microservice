package nl.tudelft.sem.template.example.domain.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * A DDD service for hashing passwords.
 */
public class PasswordHashingService {
    public static HashedPassword hash(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return new HashedPassword(encoder.encode(password));
    }
}
