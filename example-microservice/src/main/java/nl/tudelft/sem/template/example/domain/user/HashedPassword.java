package nl.tudelft.sem.template.example.domain.user;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A DDD value object representing a hashed password in our domain.
 */
@EqualsAndHashCode
@Data
public class HashedPassword {
    private final transient String hash;

    public HashedPassword(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }

    @JsonValue
    public String getHash() {
        return hash;
    }
}
