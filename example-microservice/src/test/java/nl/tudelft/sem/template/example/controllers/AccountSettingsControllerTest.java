package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettingsRepository;
import nl.tudelft.sem.template.example.domain.accountsettings.NOTIFICATIONS;
import nl.tudelft.sem.template.example.domain.accountsettings.PRIVACY;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class AccountSettingsControllerTest {

    private static AccountSettingsRepository accountSettingsRepository;
    private static UserRepository userRepository;

    private static AccountSettingsController sut;

    @BeforeAll
    public static void setup() {
        accountSettingsRepository = Mockito.mock(AccountSettingsRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        sut = new AccountSettingsController(accountSettingsRepository, userRepository);
    }
    @Test
    public void getAccountSettingsNullParameter1() {
        assertEquals(sut.getAccountSettings(null, 1), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void getAccountSettingsNullParameter2() {
        assertEquals(sut.getAccountSettings(1, null), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void getAccountSettingsNoSettings() {
        when(userRepository.findById(100)).thenReturn(Optional.of(new User()));
        when(accountSettingsRepository.findById(2)).thenReturn(Optional.empty());
        assertEquals(sut.getAccountSettings(100, 2), new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getAccountSettingsWithNegativeID() {
        when(userRepository.findById(100)).thenReturn(Optional.of(new User()));
        AccountSettings badAccountSettings = new AccountSettings(-1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, true);
        when(accountSettingsRepository.findById(2)).thenReturn(Optional.of(badAccountSettings));
        assertEquals(sut.getAccountSettings(100, 2), new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void getAccountSettingsBadID(){
        AccountSettings nonzeroID = new AccountSettings();
        nonzeroID.setId(567210);
        User user = new User();
        user.setAccountSettings(nonzeroID);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(accountSettingsRepository.findById(567210)).thenReturn(Optional.of(nonzeroID));
        assertEquals(HttpStatus.OK, sut.getAccountSettings(1, 567210).getStatusCode());

        AccountSettings negativeID = new AccountSettings();
        negativeID.setId(-1);
        when(accountSettingsRepository.findById(567212)).thenReturn(Optional.of(negativeID));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.getAccountSettings(1, 567212).getStatusCode());
    }

    @Test
    public void getAccountSettingsOK() {
        User user = new User();
        when(userRepository.findById(100)).thenReturn(Optional.of(user));
        AccountSettings accountSettings = new AccountSettings(2, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, true);
        user.setAccountSettings(accountSettings);
        when(accountSettingsRepository.findById(2)).thenReturn(Optional.of(accountSettings));
        assertEquals(sut.getAccountSettings(100, 2), new ResponseEntity<AccountSettings>(accountSettings, HttpStatus.OK));
    }
    @Test
    public void testUpdateNullParameter1() throws Exception{
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(10000, null), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testUpdateNullParameter2() throws Exception{
        AccountSettings accountSettings = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(null, accountSettings), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testUpdateAllOk() throws Exception{
        AccountSettings accountSettings = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        User user = new User("update", "update@mail.com", "update");
        user.setAccountSettings(accountSettings);
        when(userRepository.findById(1234)).thenReturn(Optional.of(user));
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(1234, accountSettings), new ResponseEntity<>(HttpStatus.OK));
    }

    @Test
    public void hackerTriesAccountNotCorrespondingToUser() throws Exception{
        AccountSettings accountSettingsSet = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        AccountSettings accountSettingsReturned = new AccountSettings(2, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        User user = new User("update", "update@mail.com", "update");
        user.setAccountSettings(accountSettingsSet);
        when(userRepository.findById(1234)).thenReturn(Optional.of(user));
        assertEquals(sut.userUserIDUpdateAccountSettingsPut(1234, accountSettingsReturned), new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void updateAccountSettingsExceptions(){
        when(userRepository.findById(787878)).thenThrow(new NoSuchElementException());
        assertEquals(HttpStatus.NOT_FOUND, sut.userUserIDUpdateAccountSettingsPut(787878,new AccountSettings()).getStatusCode());

        AccountSettings ac = new AccountSettings();
        ac.setId(676767);
        when(accountSettingsRepository.save(ac)).thenThrow(new IllegalArgumentException());
        User toQuery = new User();
        toQuery.setId(121212);
        toQuery.setAccountSettings(ac);
        when(userRepository.findById(121212)).thenReturn(Optional.of(toQuery));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, sut.userUserIDUpdateAccountSettingsPut(121212,ac).getStatusCode());
    }

    @Test
    void deactivateAccountNull(){
        assertEquals(HttpStatus.UNAUTHORIZED, sut.userUserIDDeactivatePut(null).getStatusCode());
    }

    @Test
    public void userUserIDDeactivateGood() {
        User toDeactivate = new User("delete", "delete@mail.com", "delete");
        AccountSettings accountSettings = new AccountSettings(420, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        toDeactivate.setAccountSettings(accountSettings);
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDeactivate));
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.OK));
        assertEquals(accountSettings.isAccountDeactivated(), true);
    }

    @Test
    public void userUserIDDeactivateDoesntExist() {
        when(userRepository.findById(10000)).thenReturn(Optional.empty());
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Test
    public void userUserIDDeactivateCouldntDeactivate() {
        User toDeactivate = new User("delete", "delete@mail.com", "delete");
        AccountSettings accountSettings = new AccountSettings(420, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        toDeactivate.setAccountSettings(accountSettings);
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDeactivate));
        doThrow(new IllegalArgumentException()).when(accountSettingsRepository).save(accountSettings);
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void userUserIDDeactivateNoAccountSettings() {
        User toDeactivate = new User("delete", "delete@mail.com", "delete");
        toDeactivate.setAccountSettings(null);
        when(userRepository.findById(10000)).thenReturn(Optional.of(toDeactivate));
        assertEquals(sut.userUserIDDeactivatePut(10000), new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void existenceCheckingFirstError() {
        when(userRepository.findById(105)).thenReturn(Optional.empty());
        assertEquals(new ResponseEntity<String>("a", HttpStatus.NOT_FOUND), sut.existenceCheckingWithCustomMessages(105, "a", "b"));
    }

    @Test
    void existenceCheckingSecondError() {
        when(userRepository.findById(106)).thenThrow(new IllegalArgumentException());
        assertEquals(new ResponseEntity<String>("b", HttpStatus.INTERNAL_SERVER_ERROR), sut.existenceCheckingWithCustomMessages(106, "a", "b"));
    }

    @Test
    void existenceCheckingOK() {
        User user = new User();
        when(userRepository.findById(107)).thenReturn(Optional.of(user));
        assertEquals(new ResponseEntity<>(user, HttpStatus.OK), sut.existenceCheckingWithCustomMessages(107, "a", "b"));
    }

}