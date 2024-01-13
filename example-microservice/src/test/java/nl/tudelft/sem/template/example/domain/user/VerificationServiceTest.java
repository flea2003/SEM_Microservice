package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.domain.exceptions.AlreadyHavePermissionsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificationServiceTest {

    @Test
    void testNullDocumentId() {
        VerificationService sut = new VerificationService();
        assertFalse(sut.isValid(null));
    }

    @Test
    void verifyAuthorRequestWorks() throws AlreadyHavePermissionsException, InvalidPasswordException {
        VerificationService sut = new VerificationService();
        User toBecomeAuthor = new User();
        toBecomeAuthor.setId(1);
        toBecomeAuthor.setIsAuthor(false);
        assertTrue(sut.verifyAuthorRequest(toBecomeAuthor,10888888));
    }
}