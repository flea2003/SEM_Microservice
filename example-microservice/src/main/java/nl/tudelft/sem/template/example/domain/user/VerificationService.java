package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyHavePermissionsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidPasswordException;

public class VerificationService {
    private final String adminPassword = "bookManiaAdminPassword@Admin";

    /**
     * Method to verify the admin identity of a user who requests to become one.
     *
     * @param user The user that makes the request
     * @param password The password provided by the user
     * @return True, iff password is valid and user is not already an admin
     * @throws InvalidPasswordException if password is incorrect
     * @throws AlreadyHavePermissionsException if user is already an admin
     */
    public Boolean verifyAdminRequest(User user, String password)
            throws InvalidPasswordException, AlreadyHavePermissionsException {
        if (password == null) {
            throw new InvalidPasswordException("Password cannot be null");
        } else if (!password.equals(adminPassword)) {
            throw new InvalidPasswordException();
        } else if (user.getIsAdmin()) {
            throw new AlreadyHavePermissionsException("You are already an admin!");
        }
        return true;
    }
}
