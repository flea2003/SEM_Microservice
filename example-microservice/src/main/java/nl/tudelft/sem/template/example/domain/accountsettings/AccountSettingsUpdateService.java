package nl.tudelft.sem.template.example.domain.accountsettings;

import org.springframework.stereotype.Service;

@Service
public class AccountSettingsUpdateService {
    private final transient AccountSettingsRepository accountSettingsRepository;

    /**
     * Constructor for the service.
     *
     * @param accountSettingsRepository - the AccountServiceRepository
     */
    public AccountSettingsUpdateService(AccountSettingsRepository accountSettingsRepository) {
        this.accountSettingsRepository = accountSettingsRepository;
    }


}
