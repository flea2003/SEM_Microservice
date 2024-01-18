package nl.tudelft.sem.template.example.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A DDD service for hashing passwords.
 */
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
public class PasswordHashingService {
    /**
     * Generate a hashed password.
     *
     * @param password Given password
     * @return hashed password
     */
    public static HashedPassword hash(String password) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return new HashedPassword(password);
        }
        messageDigest.update(password.getBytes());
        String stringHash = new String(messageDigest.digest());
        return new HashedPassword(stringHash);
    }
}
