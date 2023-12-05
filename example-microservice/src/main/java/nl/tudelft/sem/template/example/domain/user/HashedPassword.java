package nl.tudelft.sem.template.example.domain.user;

import lombok.EqualsAndHashCode;

/**
 * A DDD value object representing a hashed password in our domain.
 */
@EqualsAndHashCode
public class HashedPassword {
    private final transient String hash;

    public HashedPassword(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }
}
