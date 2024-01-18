package nl.tudelft.sem.template.example.domain.accountsettings;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.example.domain.user.User;

@Entity
@Getter
@Setter
@Table(name = "accountSettings")
public class AccountSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "privacy_pref", nullable = false)
    private PRIVACY privacy_pref;

    @Column(name = "notification_settings", nullable = false)
    private NOTIFICATIONS notification_settings;

    @Column(name = "enable2FA", nullable = false)
    private boolean enable2FA;

    @Column(name = "accountDeactivated", nullable = false)
    private boolean accountDeactivated;

    @OneToOne(mappedBy = "accountSettings")
    private User user;

    /**
     * Constructor for the class.
     *
     * @param id - the ID of the user which has the account settings
     * @param privacy_pref - the privacy preferences of the user
     * @param notification_settings - the notification settings of the user
     * @param enable2FA - whether 2-factor authentication is enabled or not
     * @param accountDeactivated - whether the account has been deactivated or not
     */
    public AccountSettings(Integer id, PRIVACY privacy_pref, NOTIFICATIONS notification_settings,
                           boolean enable2FA, boolean accountDeactivated) {
        this.id = id;
        this.privacy_pref = privacy_pref;
        this.notification_settings = notification_settings;
        this.enable2FA = enable2FA;
        this.accountDeactivated = accountDeactivated;
    }

    /**
     * Creates an empty Account Settings, with the default parameters.
     */
    public AccountSettings() {
        this.privacy_pref = PRIVACY.EVERYONE;
        this.notification_settings = NOTIFICATIONS.ALL;
        this.enable2FA = false;
        this.accountDeactivated = false;
    }

    public PRIVACY getPrivacy() {
        return privacy_pref;
    }

    public void setPrivacy(PRIVACY privacy_pref) {
        this.privacy_pref = privacy_pref;
    }

    public NOTIFICATIONS getNotifications() {
        return notification_settings;
    }

    public void setNotifications(NOTIFICATIONS notification_settings) {
        this.notification_settings = notification_settings;
    }

    public boolean isTwoFactorEnabled() {
        return enable2FA;
    }

    public void setTwoFactorAuth(boolean enable2FA) {
        this.enable2FA = enable2FA;
    }

    public boolean isAccountDeactivated() {
        return accountDeactivated;
    }

    public void setDeactivatedStatus(boolean accountDeactivated) {
        this.accountDeactivated = accountDeactivated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountSettings that = (AccountSettings) o;
        return enable2FA == that.enable2FA
                && accountDeactivated == that.accountDeactivated
                && Objects.equals(id, that.id)
                && privacy_pref == that.privacy_pref
                && notification_settings == that.notification_settings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, privacy_pref, notification_settings, enable2FA, accountDeactivated);
    }

    @Override
    public String toString() {
        return "AccountSettings{"
                + "id=" + id
                + ", privacy_pref=" + privacy_pref
                + ", notification_settings=" + notification_settings
                + ", enable2FA=" + enable2FA
                + ", accountDeactivated=" + accountDeactivated
                + '}';
    }
}
