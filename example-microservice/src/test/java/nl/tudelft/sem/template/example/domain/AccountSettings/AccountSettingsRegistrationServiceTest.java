package nl.tudelft.sem.template.example.domain.AccountSettings;

import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class AccountSettingsRegistrationServiceTest {

    private AccountSettingsRepository accountSettingsRepository;
    private AccountSettingsRegistrationService accountSettingsRegistrationService;

    @BeforeEach
    void setup(){
        accountSettingsRepository = Mockito.mock(AccountSettingsRepository.class);
        accountSettingsRegistrationService =
                new AccountSettingsRegistrationService(accountSettingsRepository);
    }

    @Test
    void registerSuccessful() throws Exception{
        AccountSettings accountSettings = new AccountSettings();
        when(accountSettingsRepository.save(accountSettings)).thenReturn(accountSettings);
        assertEquals(accountSettingsRegistrationService.registerAccountSettings(), accountSettings);
    }

    @Test
    void registerUnsuccessful() {
        AccountSettings accountSettings = new AccountSettings();
        when(accountSettingsRepository.save(accountSettings)).thenThrow(new IllegalArgumentException());
        assertThrows(InvalidUserException.class, () -> {accountSettingsRegistrationService.registerAccountSettings();});
    }

}
