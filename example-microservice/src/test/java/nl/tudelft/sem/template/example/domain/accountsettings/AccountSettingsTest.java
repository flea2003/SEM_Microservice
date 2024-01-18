package nl.tudelft.sem.template.example.domain.accountsettings;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountSettingsTest {
    private AccountSettings as;
    @BeforeEach
    void setup() {
        as = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, true, false);
    }
    @Test
    void getPrivacy() {
        assertEquals(as.getPrivacy(), PRIVACY.EVERYONE);
    }

    @Test
    void setPrivacy() {
        as.setPrivacy(PRIVACY.ONLY_ME);
        assertEquals(as.getPrivacy(), PRIVACY.ONLY_ME);
    }

    @Test
    void getNotifications() {
        assertEquals(as.getNotifications(), NOTIFICATIONS.ALL);
    }

    @Test
    void setNotifications() {
        as.setNotifications(NOTIFICATIONS.NONE);
        assertEquals(as.getNotifications(), NOTIFICATIONS.NONE);
    }

    @Test
    void isTwoFactorEnabled() {
        assertTrue(as.isTwoFactorEnabled());
    }

    @Test
    void setTwoFactorAuth() {
        as.setTwoFactorAuth(false);
        assertFalse(as.isTwoFactorEnabled());
    }

    @Test
    void isAccountDeactivated() {
        assertFalse(as.isAccountDeactivated());
    }

    @Test
    void setDeactivatedStatus() {
        as.setDeactivatedStatus(true);
        assertTrue(as.isAccountDeactivated());
    }

    @Test
    void testEqualsDifferentID() {
        AccountSettings as2 = new AccountSettings(2, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, true, false);
        assertNotEquals(as, as2);
    }

    @Test
    void testEqualsDifferentPrivacy() {
        AccountSettings as2 = new AccountSettings(1, PRIVACY.ONLY_ME, NOTIFICATIONS.ALL, true, false);
        assertNotEquals(as, as2);
    }

    @Test
    void testEqualsDifferentNotifications() {
        AccountSettings as2 = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.NONE, true, false);
        assertNotEquals(as, as2);
    }
    @Test
    void testEqualsDifferent2FA() {
        AccountSettings as2 = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, false, false);
        assertNotEquals(as, as2);
    }
    @Test
    void testEqualsDifferentDeactivationStatus() {
        AccountSettings as2 = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, true, true);
        assertNotEquals(as, as2);
    }

    @Test
    void testEqualsSameAddress() {
        AccountSettings as2 = as;
        assertEquals(as, as2);
    }

    @Test
    void testEqualsSameObjectContent() {
        AccountSettings as2 = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, true, false);
        assertEquals(as, as2);
    }

    // hashes the same when content is the same
    @Test
    void testHashCode() {
        AccountSettings as2 = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, true, false);
        assertEquals(as.hashCode(), as2.hashCode());

        assertNotEquals(0, as.hashCode());
    }

    @Test
    void testEqualsDiffClass(){
        AccountSettings as2 = new AccountSettings(1, PRIVACY.EVERYONE, NOTIFICATIONS.ALL, true, false);
        assertNotEquals(as2,new User());
    }

    @Test
    void testToString() {
        assertEquals(as.toString(), "AccountSettings{id=1, privacy_pref=EVERYONE, notification_settings=ALL, enable2FA=true, accountDeactivated=false}");
    }
}