package nl.tudelft.sem.template.example.domain.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A DDD service for hashing passwords.
 */
public class PasswordHashingService {
    public static HashedPassword hash(String password) {
        MessageDigest messageDigest = null;
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
