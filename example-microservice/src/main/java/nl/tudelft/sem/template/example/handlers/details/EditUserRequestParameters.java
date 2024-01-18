package nl.tudelft.sem.template.example.handlers.details;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetails;

@Getter
@EqualsAndHashCode
public class EditUserRequestParameters {
    private final Integer userId;
    private final UserDetails userDetails;

    public EditUserRequestParameters(Integer userId, UserDetails userDetails) {
        this.userId = userId;
        this.userDetails = userDetails;
    }
}
