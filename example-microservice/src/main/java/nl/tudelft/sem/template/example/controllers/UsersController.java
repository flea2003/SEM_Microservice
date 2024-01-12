package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettingsRegistrationService;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettingsRepository;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettingsUpdateService;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.analytics.AnalyticsService;
import nl.tudelft.sem.template.example.domain.exceptions.AlreadyHavePermissionsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidPasswordException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.domain.user.VerificationService;

import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.domain.UserDetails.*;
import nl.tudelft.sem.template.example.models.UserPostRequest;
import nl.tudelft.sem.template.example.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Users controller.
 * <p>
 * This controller handles actions related to users.
 * </p>
 */
@RestController
public class UsersController {
    UserRegistrationService userRegistrationService;
    UserDetailsRegistrationService userDetailsRegistrationService;
    AccountSettingsRegistrationService accountSettingsRegistrationService;
    UserRepository userRepository;
    UserDetailsRepository userDetailsRepository;
    AccountSettingsRepository accountSettingsRepository;
    VerificationService verificationService;
    UpdateUserService updateUserService;
    AnalyticsService analyticsService;
    UpdateUserDetailsService updateUserDetailsService;


    @Autowired
    public UsersController(UserRegistrationService userRegistrationService, UpdateUserService updateUserService,
                           UserRepository userRepository, UserDetailsRepository userDetailsRepository,
                           AccountSettingsRepository accountSettingsRepository,
                           AccountSettingsRegistrationService accountSettingsRegistrationService,
                           UpdateUserDetailsService updateUserDetailsService,
                           UserDetailsRegistrationService userDetailsRegistrationService,
                           AnalyticsService analyticsService) {
        this.userRegistrationService = userRegistrationService;
        this.updateUserService = updateUserService;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.verificationService = new VerificationService();
        this.updateUserDetailsService = updateUserDetailsService;
        this.accountSettingsRepository = accountSettingsRepository;
        this.userDetailsRegistrationService = userDetailsRegistrationService;
        this.analyticsService = analyticsService;
        this.accountSettingsRegistrationService = accountSettingsRegistrationService;
    }

    /**
     * POST /user : Creates a new user and logs them in.
     *
     * @param userPostRequest  (required)
     * @return User was created and logged in successfully (status code 200)
     *         or Request body is malformed (status code 400)
     *         or User with username/email already exists (status code 409)
     *         or Internal registration failure (status code 500)
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/user",
            consumes = { "application/json" }
    )
    public ResponseEntity<String> userPost(@Parameter(name = "UserPostRequest", required = true) @Valid
                                             @RequestBody UserPostRequest userPostRequest) {

        String username = userPostRequest.getUsername();
        String email = userPostRequest.getEmail();
        String password = userPostRequest.getPassword();

        //Invalid input in request
        if (username == null || username.isEmpty()
                || email == null || email.isEmpty()
                || password == null || password.isEmpty()) {
            return new ResponseEntity<>("Request body is malformed", HttpStatus.BAD_REQUEST);
        }

        //User already exists with same email
        if (userRegistrationService.getUserByEmail(email) != null) {
            return new ResponseEntity<>("User with email already exists", HttpStatus.CONFLICT);
        }

        UserDetails toAddDetails;
        try {
            toAddDetails = userDetailsRegistrationService.registerUserDetails();
        } catch (InvalidUserException e) {
            return new ResponseEntity<>("Couldn't register user", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        AccountSettings toAddSettings;
        try{
            toAddSettings = accountSettingsRegistrationService.registerAccountSettings();
        }catch (InvalidUserException e){
            return new ResponseEntity<>("Couldn't register user", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User toAdd;
        //User details are present -> try to save it to the database
        try {
            toAdd = userRegistrationService.registerUser(username, email, password, toAddDetails, toAddSettings);
        } catch (InvalidUserException e1) {
            return new ResponseEntity<>("Username or email format was incorrect", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Database insertion failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Logged in user ID", String.valueOf(toAdd.getId()));
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body("User created successfully");
    }

    /**
     * POST /login : Logs in the user.
     *
     * @param loginPostRequest  (required)
     * @return User was logged in successfully (status code 200)
     *         or Request body is malformed (status code 400)
     *         or Invalid username or password (status code 401)
     *         or Internal login failure (status code 500)
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/login",
            consumes = { "application/json" }
    )
    public ResponseEntity<User> loginUser(@Parameter(name = "LoginPostRequest", required = true) @Valid
                                              @RequestBody LoginPostRequest loginPostRequest) {
        if (loginPostRequest == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String username = loginPostRequest.getUsername();
        String password = loginPostRequest.getPassword();

        //Invalid input in request
        if (username == null || username.isEmpty()
                || password == null || password.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Try to access the database
        List<User> users;
        try {
            users = userRegistrationService.getUserByUsername(username);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (users == null || users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Find user with given password
        for (User u : users) {
            if (u.getPassword().toString().equals(PasswordHashingService.hash(password).toString())) {
                return new ResponseEntity<>(u, HttpStatus.OK);
            }
        }

        //Password was not correct
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /**
     * POST /user/{userID}/makeAdmin : Gives the user admin privileges.
     *
     * @param userId ID of user that makes the request
     * @param password Password input by user
     * @return User with specific ID cannot be found (code 404)
     *         Something went wrong while retrieving the user (code 500)
     *         Password is null or invalid (code 401)
     *         User is already an admin (code 409)
     *         User has successfully become an admin (code 200)
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/user/{userID}/makeAdmin",
            consumes = { "text/plain" }
    )
    public ResponseEntity<String> makeAdmin(@PathVariable(name = "userID") int userId,
                                            @RequestBody String password) {
        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Username with that ID could not be found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            verificationService.verifyAdminRequest(user, password);
        } catch (InvalidPasswordException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (AlreadyHavePermissionsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }

        String message = "User with ID:" + userId + " is now an admin";
        user.setIsAdmin(true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be updated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * GET /user/{userID} : Fetch user information.
     *
     * @param userID Numeric ID of the user that makes the request (required)
     * @return User data fetched successfully (status code 200)
     *         or User is not authenticated (status code 401)
     *         or Internal server error (status code 500)
     */
    @GetMapping(value = "/user/{userID}")
    public ResponseEntity<User> userGetUser(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true)
            @PathVariable("userID") Integer userID) {

        User user;
        try {
            user = userRegistrationService.getUserById(userID);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * PUT /user/{userID}/changePassword : Change user password.
     *
     * @param userId Numeric ID of the user that makes the request (required)
     * @param body  The desired password (required)
     * @return Password changed successfully (status code 200)
     *         or Request body is malformed (status code 401)
     *         or Password could not be changed (status code 500)
     */
    @PutMapping(value = "/user/{userID}/changePassword")
    public ResponseEntity<String> userChangePassword(
            @Parameter(name = "userID", required = true) @PathVariable("userID") Integer userId,
            @Parameter(name = "body", description = "Desired Password", required = true) @Valid @RequestBody String body
    ) {
        if (userId == null || body.isEmpty()) {
            return new ResponseEntity<>("Request body is malformed", HttpStatus.BAD_REQUEST);
        }

        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User with that ID could not be found", HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HashedPassword hashedPassword = PasswordHashingService.hash(body);
        User updatedUser;

        try {
            updatedUser = updateUserService.changePassword(user.getId(), hashedPassword);
            if (updatedUser == null) {
                throw new NoSuchElementException();
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Couldn't change the password", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Database insertion failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Your password has been changed successfully.", HttpStatus.OK);
    }


    /**
     *
     * GET user/{userID}/userDetails/{userDetails}
     * @param userID - Numeric ID of the user that makes the request
     * @param userDetailsID - Numeric ID of the userDetails that are requested
     * @return Unauthorised access to details (status code 401)
     *         Details not found (status code 404)
     *         User details cannot be accessed (status code 500)
     *         User details fetched successfully (status code 200) + userDetails
     */
    @GetMapping(value = "/user/{userID}/userDetails/{userDetailsID}")
    public ResponseEntity<UserDetails> getUserDetails(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID,
            @Parameter(name = "userDetailsID", description = "ID of the details that are requested", required = true, in = ParameterIn.PATH) @PathVariable("userDetailsID") Integer userDetailsID
    ) {
        if(userID == null || userDetailsID == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user;
        try {
            user = userRegistrationService.getUserById(userID);
            if(user == null){
                throw new InvalidUserException();
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails;
        try{
            Optional<UserDetails> optionalUserDetails = userDetailsRepository.findById(userDetailsID);
            if (optionalUserDetails.isEmpty()) {
                throw new NoSuchElementException();
            }
            if(optionalUserDetails.get().getId() < 0){
                throw new IllegalArgumentException();
            }
            userDetails = optionalUserDetails.get();
        }catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(userDetails, HttpStatus.OK);
    }

    /**
     * PUT /user/{userID}/editUser : Edit the user's details.
     * @param userID Numeric ID of the user that makes the request (required)
     * @return User details updated successfully (status code 200)
     *         or Unauthorised changes to the user (status code 401)
     *         or User could not be found (status code 404)
     *         or User could not be updated or new data is invalid (status code 500)
     */
    @PutMapping(value = "/user/{userID}/editUser")
    public ResponseEntity<String> editUserDetails(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID,
            @RequestBody UserDetails details)
    {
        if (userID == null || details == null)
            return new ResponseEntity<>("Request is malformed", HttpStatus.BAD_REQUEST);
        User user = userRegistrationService.getUserById(userID);
        if (user == null) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        try {
            updateUserDetailsService.updateUserDetails(user.getId(), details);
        }
        catch (InvalidUserDetailsException e) {
            return new ResponseEntity<>("User could not be updated or new data is invalid", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (Exception e) {
            return new ResponseEntity<>("Unauthorised changes to the user", HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>("User details updated successfully", HttpStatus.OK);
    }

    /**
     * POST /user/{userID}/makeAuthor - Give the user author privileges.
     *
     * @param userId Id of the user that makes the request.
     * @param document document object provided by the user for verification.
     * @return Request body is malformed (code 400)
     *         User with provided ID could not be found (code 404)
     *         Database error (code 500)
     *         User is already an author (code 409)
     *         Document is invalid (code 401)
     *         User has successfully become an author (code 200)
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/user/{userID}/makeAuthor",
            consumes = { "application/json" }
    )
    public ResponseEntity<String> makeAuthor(@PathVariable(name = "userID") int userId,
                                             @RequestBody DocumentConversionRequest document) {
        if (document == null || document.getDocumentID() == null) {
            return new ResponseEntity<>("Request body is malformed", HttpStatus.BAD_REQUEST);
        }
        int documentId = document.getDocumentID();

        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User with that ID could not be found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            verificationService.verifyAuthorRequest(user, documentId);
        } catch (AlreadyHavePermissionsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (InvalidPasswordException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        String message = "User with ID:" + userId + " is now an author";
        user.setIsAuthor(true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be saved", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * DELETE /user/{userID}/delete/{} : User wants to delete their account by own choice.
     *
     * @param userID Numeric ID of the user that makes the request (required)
     * @return User account deletion successful (status code 200)
     *         or User not logged in (status code 401)
     *         or User not found (status code 404)
     *         or User account could not be deleted (status code 500)
     */
    @Operation(
            operationId = "userUserIDDelete",
            summary = "User wants to delete their account by own choice.",
            tags = { "User Operations" },
            responses = {
                    @ApiResponse(responseCode = "200", description = "User account deletion successful"),
                    @ApiResponse(responseCode = "401", description = "User not logged in"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "User account could not be deleted")
            }
    )
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/user/{userID}"
    )
    public ResponseEntity<Void> userUserIDDelete(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID
    ) {
        if(userID == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //given this api, there is no way to verify whether an user is logged in or not ...
        //should we modify the api in order to also include in the url the id of the user making the request???
        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userID);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            userRepository.delete(user);
        } catch (Exception e) {
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
        if(userID == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        //given this api, there is no way to verify whether an user is logged in or not ...
        //should we modify the api in order to also include in the url the id of the user making the request???
        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userID);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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



    /**
     * GET /user/{userID}/search/{name} : Search for users based on a query.
     *
     * @param userId The id of the searcher user (required)
     * @param name The search query (required)
     * @return Users matching the search query (status code 200)
     *         or No users found (status code 404)
     *         or Internal server error (status code 500)
     */
    @GetMapping(value = "/user/{userID}/search/{name}")
    public ResponseEntity<List<User>> userSearch(
            @Parameter(name = "userID", required = true) @PathVariable("userID") Integer userId,
            @Parameter(name = "name", description = "Name of the searched user", required = true)
            @PathVariable String name) {


        if (name == null || name.isEmpty() || userId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
            List<User> users;
            users = userRegistrationService.getUserByUsername(name);

            if (!users.isEmpty()) {
                return new ResponseEntity<>(users, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
        User user;
        try{
            Optional<User>optionalUser = userRepository.findById(userID);
            user = optionalUser.get();
        }catch (NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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


}
