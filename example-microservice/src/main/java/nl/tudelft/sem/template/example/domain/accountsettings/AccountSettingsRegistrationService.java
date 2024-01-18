package nl.tudelft.sem.template.example.domain.accountsettings;

import java.util.Optional;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import org.springframework.stereotype.Service;

/**
 * A DDD service for creating a new account setting for a new user.
 */
@Service
public class AccountSettingsRegistrationService {

    private final transient AccountSettingsRepository accountSettingsRepository;

    /**
     * Instantiates a new AccountSettingsCreationService.
     *
     * @param accountSettingsRepository - the accountSettings repository
    */
    public AccountSettingsRegistrationService(AccountSettingsRepository accountSettingsRepository) {
        this.accountSettingsRepository = accountSettingsRepository;
    }

    /**
     * This method registers a new AccountSettings to the database.
     *
     * @return The registered AccountSettings or Exceptions if insertion failed
     */
    public AccountSettings registerAccountSettings() throws InvalidUserException {
        AccountSettings accountSettings = new AccountSettings();
        try {
            return accountSettingsRepository.save(accountSettings);
        } catch (Exception e) {
            throw new InvalidUserException("Couldn't register user");
        }
    }

    /**
     * Returns an AccountSettings object by ID.
     *
     * @param anyId the ID of the object to search
     * @return an optional of the object from the repository
     */
    public Optional<AccountSettings> findById(Integer anyId) {
        return accountSettingsRepository.findById(anyId);
    }
}
