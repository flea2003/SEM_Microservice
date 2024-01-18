package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.validation.Valid;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettingsRepository;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.strategy.Authentication;
import nl.tudelft.sem.template.example.strategy.UserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handles actions to account settings.
 */
@RestController
public class AccountSettingsController {
    AccountSettingsRepository accountSettingsRepository;
    UserRepository userRepository;

    /**
     * Constructor for the account settings controller.
     */
    @Autowired
    public AccountSettingsController(AccountSettingsRepository accountSettingsRepository, UserRepository userRepository) {
        this.accountSettingsRepository = accountSettingsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Utility function for error checking when trying to retrieve a user from the database.
     *
     * @param userId   the ID of the user to retrieve
     * @param message1 the first error message, corresponding to the first error status code
     * @param message2 the second error message, corresponding to the second error status code
     * @return ResponseEntity of either a String with the error message, or the actual User retrieved
     */
    public ResponseEntity<? extends Object> existenceCheckingWithCustomMessages(Integer userId, String message1,
                                                                                String message2) {
        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(message1, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(message2, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * GET user/{userID}/userDetails/{accountSettingsID}.
     *
     * @param userId            - Numeric ID of the user that makes the request
     * @param accountSettingsId - ID of the account settings that are requested
     * @return Unauthorised access to account settings (status code 401)
     *      Account settings not found (status code 404)
     *      User account settings cannot be accessed (status code 500)
     *      User account settings fetched successfully (status code 200) and AccountSettings entity
     */
    //@GetMapping(value = "/user/{userID}/userDetails/{accountSettingsID}")
    public ResponseEntity<AccountSettings> getAccountSettings(
        @Parameter(name = "userID", description = "Numeric ID of the user that makes the request",
            required = true, in = ParameterIn.PATH)
        @PathVariable("userID") Integer userId,
        @Parameter(name = "accountSettingsID", description = "ID of the account settings that are requested",
            required = true, in = ParameterIn.PATH)
        @PathVariable("accountSettingsID") Integer accountSettingsId
    ) {
        if (userId == null || accountSettingsId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>(checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();

        AccountSettings accountSettings;
        try {
            Optional<AccountSettings> optionalAccountSettings = accountSettingsRepository.findById(accountSettingsId);
            if (optionalAccountSettings.isEmpty()) {
                throw new NoSuchElementException();
            }
            if (optionalAccountSettings.get().getId() < 0) {
                throw new IllegalArgumentException();
            }
            accountSettings = optionalAccountSettings.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //check if the ids are the same

        Authentication authentication = new UserAuthentication(user.getAccountSettings().getId(), accountSettingsId);
        if (authentication.authenticate()) {
            return new ResponseEntity<>(accountSettings, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(accountSettings, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * PUT /user/{userId}/updateAccountSettings : Update the account settings for the logged in user.
     *
     * @param userId          Numeric ID of the user that makes the request (required)
     * @param accountSettings (required)
     * @return Account settings changed successfully (status code 200)
     *      or User not logged in (status code 401)
     *      or User not found (status code 404)
     *      or Account settings could not be changed (status code 500)
     */
    @Operation(
        operationId = "userUserIDUpdateAccountSettingsPut",
        summary = "Update the account settings for the logged in user",
        tags = {"User Operations"},
        responses = {
            @ApiResponse(responseCode = "200", description = "Account settings changed successfully"),
            @ApiResponse(responseCode = "401", description = "User not logged in"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Account settings could not be changed")
        }
    )
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/user/{userID}/updateAccountSettings",
        consumes = {"application/json"}
    )
    public ResponseEntity<Void> userUserIdUpdateAccountSettingsPut(
        @Parameter(name = "userID", description = "Numeric ID of the user that makes the request",
            required = true, in = ParameterIn.PATH)
        @PathVariable("userID") Integer userId,
        @Parameter(name = "AccountSettings", description = "", required = true) @Valid @RequestBody
        AccountSettings accountSettings
    ) {
        if (userId == null || accountSettings == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>(checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();
        if (user.getAccountSettings().getId() != accountSettings.getId()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            accountSettingsRepository.save(accountSettings);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * PUT /user/{userID}/deactivate : User wants to deactivate their account by own choice.
     * Set their status as &#39;deactivated&#39;.
     *
     * @param userId Numeric ID of the user that makes the request (required)
     * @return User account deactivation successful (status code 200)
     *      or User not logged in (status code 401)
     *      or User not found (status code 404)
     *      or User account could not be deactivated (status code 500)
     */
    @Operation(
        operationId = "userUserIDDeactivatePut",
        summary = "User wants to deactivate their account by own choice. Set their status as 'deactivated'.",
        tags = {"User Operations"},
        responses = {
            @ApiResponse(responseCode = "200", description = "User account deactivation successful"),
            @ApiResponse(responseCode = "401", description = "User not logged in"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "User account could not be deactivated")
        }
    )
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/user/{userID}/deactivate"
    )
    public ResponseEntity<Void> userUserIdDeactivatePut(
        @Parameter(name = "userID", description = "Numeric ID of the user that makes the request",
            required = true, in = ParameterIn.PATH)
        @PathVariable("userID") Integer userId
    ) {
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //given this api, there is no way to verify whether an user is logged in or not ...
        //should we modify the api in order to also include in the url the id of the user making the request???
        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>(checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();
        try {
            if (user.getAccountSettings() == null) {
                throw new Exception();
            }
            AccountSettings accountSettings = user.getAccountSettings();
            accountSettings.setAccountDeactivated(true);
            accountSettingsRepository.save(accountSettings);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}