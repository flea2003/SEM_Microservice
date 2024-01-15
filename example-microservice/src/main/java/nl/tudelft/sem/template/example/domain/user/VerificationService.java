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

    /**
     * Method that validates a documentId.
     * Currently, we consider only documents whose Ids length is 8, and they start with "10".
     *
     * @param documentId The Id to be validated.
     * @return True, iff the Id is valid.
     */
    public Boolean isValid(Integer documentId) {
        if(documentId == null)
            return false;

        String id = String.valueOf(documentId);
        return id.length() == 8 && id.startsWith("10");
    }

    /**
     * Method to verify the author identity of a user who requests to become one.
     *
     * @param user The user that made the request.
     * @param documentId The Id of the document submitted by the user.
     * @return true, iff the document is valid and the user is not already an author.
     * @throws AlreadyHavePermissionsException if the user is already an author.
     * @throws InvalidPasswordException if the document is invalid.
     */
    public Boolean verifyAuthorRequest(User user, Integer documentId)
            throws AlreadyHavePermissionsException, InvalidPasswordException {
        if (!isValid(documentId)) {
            throw new InvalidPasswordException("Document not valid");
        } else if (user.getIsAuthor()) {
            throw new AlreadyHavePermissionsException("You are already an author!");
        }
        return true;

    }
}
