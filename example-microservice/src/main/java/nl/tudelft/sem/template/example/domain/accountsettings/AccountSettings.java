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
    private Privacy privacyPref;

    @Column(name = "notification_settings", nullable = false)
    private Notifications notificationSettings;

    @Column(name = "enable2FA", nullable = false)
    private boolean enable2Fa;

    @Column(name = "accountDeactivated", nullable = false)
    private boolean accountDeactivated;

    @OneToOne(mappedBy = "accountSettings")
    private User user;

    /**
     * Constructor for the class.
     *
     * @param id - the ID of the user which has the account settings
     * @param privacyPref - the privacy preferences of the user
     * @param notificationSettings - the notification settings of the user
     * @param enable2Fa - whether 2-factor authentication is enabled or not
     * @param accountDeactivated - whether the account has been deactivated or not
     */
    public AccountSettings(Integer id, Privacy privacyPref, Notifications notificationSettings,
                           boolean enable2Fa, boolean accountDeactivated) {
        this.id = id;
        this.privacyPref = privacyPref;
        this.notificationSettings = notificationSettings;
        this.enable2Fa = enable2Fa;
        this.accountDeactivated = accountDeactivated;
    }

    /**
     * Creates an empty Account Settings, with the default parameters.
     */
    public AccountSettings() {
        this.privacyPref = Privacy.EVERYONE;
        this.notificationSettings = Notifications.ALL;
        this.enable2Fa = false;
        this.accountDeactivated = false;
    }

    public Privacy getPrivacy() {
        return privacyPref;
    }

    public void setPrivacy(Privacy privacyPref) {
        this.privacyPref = privacyPref;
    }

    public Notifications getNotifications() {
        return notificationSettings;
    }

    public void setNotifications(Notifications notificationSettings) {
        this.notificationSettings = notificationSettings;
    }

    public boolean isTwoFactorEnabled() {
        return enable2Fa;
    }

    public void setTwoFactorAuth(boolean enable2Fa) {
        this.enable2Fa = enable2Fa;
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
        return enable2Fa == that.enable2Fa
                && accountDeactivated == that.accountDeactivated
                && Objects.equals(id, that.id)
                && privacyPref == that.privacyPref
                && notificationSettings == that.notificationSettings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, privacyPref, notificationSettings, enable2Fa, accountDeactivated);
    }

    @Override
    public String toString() {
        return "AccountSettings{"
                + "id=" + id
                + ", privacy_pref=" + privacyPref
                + ", notification_settings=" + notificationSettings
                + ", enable2FA=" + enable2Fa
                + ", accountDeactivated=" + accountDeactivated
                + '}';
    }
}
