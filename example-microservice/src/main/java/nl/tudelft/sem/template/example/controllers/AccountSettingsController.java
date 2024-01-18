package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettingsRepository;
import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.strategy.Authentication;
import nl.tudelft.sem.template.example.strategy.UserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Controller handles actions to account settings.
 */
@RestController
public class AccountSettingsController {
    AccountSettingsRepository accountSettingsRepository;
    UserRepository userRepository;

    /**
     * Utility function for error checking when trying to retrieve a user from the database.
     * @param userID the ID of the user to retrieve
     * @param message1 the first error message, corresponding to the first error status code
     * @param message2 the second error message, corresponding to the second error status code
     * @return ResponseEntity of either a String with the error message, or the actual User retrieved
     */
    public ResponseEntity<? extends Object> existenceCheckingWithCustomMessages(Integer userID, String message1, String message2) {
        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userID);
            if(optionalUser.isEmpty())
                throw new NoSuchElementException();
            user = optionalUser.get();
        }
        catch (NoSuchElementException e) {
            return new ResponseEntity<>(message1, HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(message2, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Constructor for the account settings controller.
     */
    @Autowired
    public AccountSettingsController(AccountSettingsRepository accountSettingsRepository, UserRepository userRepository) {
        this.accountSettingsRepository = accountSettingsRepository;
        this.userRepository = userRepository;
    }

    /**
     *
     * GET user/{userID}/userDetails/{accountSettingsID}
     * @param userID - Numeric ID of the user that makes the request
     * @param accountSettingsID - ID of the account settings that are requested
     * @return Unauthorised access to account settings (status code 401)
     *         Account settings not found (status code 404)
     *         User account settings cannot be accessed (status code 500)
     *         User account settings fetched successfully (status code 200) and AccountSettings entity
     */
    //@GetMapping(value = "/user/{userID}/userDetails/{accountSettingsID}")
    public ResponseEntity<AccountSettings> getAccountSettings(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID,
            @Parameter(name = "accountSettingsID", description = "ID of the account settings that are requested", required = true, in = ParameterIn.PATH) @PathVariable("accountSettingsID") Integer accountSettingsID
    ) {
        if(userID == null || accountSettingsID == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        ResponseEntity<?> checkUserIDResult = existenceCheckingWithCustomMessages(userID, "User with that ID could not be found", "Something went wrong");
        if (checkUserIDResult.getBody() instanceof String)
            return new ResponseEntity<>(checkUserIDResult.getStatusCode());
        User user = (User) checkUserIDResult.getBody();

        AccountSettings accountSettings;
        try {
            Optional<AccountSettings> optionalAccountSettings = accountSettingsRepository.findById(accountSettingsID);
            if (optionalAccountSettings.isEmpty()) {
                throw new NoSuchElementException();
            }
            if(optionalAccountSettings.get().getId() < 0) {
                throw new IllegalArgumentException();
            }
            accountSettings = optionalAccountSettings.get();
        }
        catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //check if the ids are the same

        Authentication authentication = new UserAuthentication(user.getAccountSettings().getId(), accountSettingsID);
        if(authentication.authenticate()) {
            return new ResponseEntity<>(accountSettings, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(accountSettings, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * PUT /user/{userID}/updateAccountSettings : Update the account settings for the logged in user
     *
     * @param userID Numeric ID of the user that makes the request (required)
     * @param accountSettings  (required)
     * @return Account settings changed successfully (status code 200)
     *         or User not logged in (status code 401)
     *         or User not found (status code 404)
     *         or Account settings could not be changed (status code 500)
     */
    @Operation(
            operationId = "userUserIDUpdateAccountSettingsPut",
            summary = "Update the account settings for the logged in user",
            tags = { "User Operations" },
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
            consumes = { "application/json" }
    )
    public ResponseEntity<Void> userUserIDUpdateAccountSettingsPut(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID,
            @Parameter(name = "AccountSettings", description = "", required = true) @Valid @RequestBody AccountSettings accountSettings
    ) {
        if(userID == null || accountSettings == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<?> checkUserIDResult = existenceCheckingWithCustomMessages(userID, "User with that ID could not be found", "Something went wrong");
        if (checkUserIDResult.getBody() instanceof String)
            return new ResponseEntity<>(checkUserIDResult.getStatusCode());
        User user = (User) checkUserIDResult.getBody();
        if(user.getAccountSettings().getId() != accountSettings.getId()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try{
            accountSettingsRepository.save(accountSettings);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * PUT /user/{userID}/deactivate : User wants to deactivate their account by own choice. Set their status as &#39;deactivated&#39;.
     *
     * @param userID Numeric ID of the user that makes the request (required)
     * @return User account deactivation successful (status code 200)
     *         or User not logged in (status code 401)
     *         or User not found (status code 404)
     *         or User account could not be deactivated (status code 500)
     */
    @Operation(
            operationId = "userUserIDDeactivatePut",
            summary = "User wants to deactivate their account by own choice. Set their status as 'deactivated'.",
            tags = { "User Operations" },
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
    public ResponseEntity<Void> userUserIDDeactivatePut(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID
    ) {
        if(userID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //given this api, there is no way to verify whether an user is logged in or not ...
        //should we modify the api in order to also include in the url the id of the user making the request???
        ResponseEntity<?> checkUserIDResult = existenceCheckingWithCustomMessages(userID, "User with that ID could not be found", "Something went wrong");
        if (checkUserIDResult.getBody() instanceof String)
            return new ResponseEntity<>(checkUserIDResult.getStatusCode());
        User user = (User) checkUserIDResult.getBody();
        try {
            if(user.getAccountSettings() == null) {
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