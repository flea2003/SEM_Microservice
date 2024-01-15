package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettingsRegistrationService;
import nl.tudelft.sem.template.example.domain.AccountSettings.AccountSettingsRepository;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.analytics.AnalyticsService;
import nl.tudelft.sem.template.example.domain.exceptions.*;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.domain.user.VerificationService;

import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.domain.UserDetails.*;
import nl.tudelft.sem.template.example.handlers.Validator;
import nl.tudelft.sem.template.example.handlers.details.EditUserRequestParameters;
import nl.tudelft.sem.template.example.handlers.details.NullFieldsValidator;
import nl.tudelft.sem.template.example.handlers.details.RequestUserValidator;
import nl.tudelft.sem.template.example.handlers.details.UserDetailsValidator;
import nl.tudelft.sem.template.example.handlers.userCreation.*;
import nl.tudelft.sem.template.example.models.UserPostRequest;
import nl.tudelft.sem.template.example.models.*;
import nl.tudelft.sem.template.example.strategy.Authentication;
import nl.tudelft.sem.template.example.strategy.UserAuthentication;
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
        // Create Chain
        Validator<UserPostRequest> handler = new NullOrEmptyFieldsValidator<>();
        //handler.setNextOperation(new EmailFormatValidator<>());
        handler.link(new EmailFormatValidator<>(), new UsernameFormatValidator<>(), new NoSameEmailUserValidator<>(userRegistrationService));
        // Handle exceptions
        try {
            handler.handle(userPostRequest);
        } catch(InputFormatException e) {
            if (e instanceof MalformedBodyException || e instanceof InvalidEmailException || e instanceof InvalidUsernameException)
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            else if (e instanceof AlreadyExistsException)
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
        // Handle potential DB failures
        UserDetails toAddDetails;
        AccountSettings toAddSettings;
        User toAdd;
        try {
            toAddDetails = userDetailsRegistrationService.registerUserDetails();
            toAddSettings = accountSettingsRegistrationService.registerAccountSettings();
            toAdd = userRegistrationService.registerUser(userPostRequest.getUsername(), userPostRequest.getEmail(),
                    userPostRequest.getPassword(), toAddDetails, toAddSettings);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
     * GET user/{userID}/userDetails/{anyID}
     *
     * This serves as a decision function between the 2 endpoints which have the same string path, the request can't
     * distinguish based on a path parameter, because they are both Integers.
     * First looks whether the ID corresponds to a UserDetails instance or an AccountSettings one, then calls the
     * relevant endpoint.
     * @param userID Numeric ID of the user that makes the request
     * @param anyID ID of either the account settings or user details that are requested
     * @return Unauthorised access (status code 401)
     *         Neither UserDetails nor AccountSettings found (status code 404)
     *         User details or account settings fetched successfully (status code 200) and relevant entity
     */
    @GetMapping(value = "/user/{userID}/userDetails/{anyID}")
    public ResponseEntity<? extends Object> getUserDetailsOrAccountSettings(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID,
            @Parameter(name = "anyID", description = "ID of either the account settings or user details that are requested", required = true, in = ParameterIn.PATH) @PathVariable("anyID") Integer anyID
    ) {
        // basic checking beforehand
        if(userID == null || anyID == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        User user;
        try {
            user = userRegistrationService.getUserById(userID);
            if(user == null) {
                throw new InvalidUserException();
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<? extends Object> optional = userDetailsRepository.findById(anyID); // first look whether it was a UserDetails request
        if (optional.isEmpty()) {
            optional = accountSettingsRepository.findById(anyID);
            if (optional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            else { // it was AccountSettings
                return getAccountSettings(userID, anyID);
            }
        }
        else { // it was UserDetails
            return getUserDetails(userID, anyID);
        }
    }

    /**
     * GET user/{userID}/userDetails/{userDetails}
     * @param userID - Numeric ID of the user that makes the request
     * @param userDetailsID - Numeric ID of the userDetails that are requested
     * @return Unauthorised access to details (status code 401)
     *         Details not found (status code 404)
     *         User details cannot be accessed (status code 500)
     *         User details fetched successfully (status code 200) + userDetails
     */
    //@GetMapping(value = "/user/{userID}/userDetails/{userDetailsID}")
    public ResponseEntity<UserDetails> getUserDetails(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID,
            @Parameter(name = "userDetailsID", description = "ID of the details that are requested", required = true, in = ParameterIn.PATH) @PathVariable("userDetailsID") Integer userDetailsID
    ) {
        if (userID == null || userDetailsID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user;
        try {
            user = userRegistrationService.getUserById(userID);
            if(user == null) {
                throw new InvalidUserException();
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails;
        try {
            Optional<UserDetails> optionalUserDetails = userDetailsRepository.findById(userDetailsID);
            if (optionalUserDetails.isEmpty()) {
                throw new NoSuchElementException();
            }
            if(optionalUserDetails.get().getId() < 0) {
                throw new IllegalArgumentException();
            }
            userDetails = optionalUserDetails.get();
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Authentication authentication = new UserAuthentication(user.getUserDetails().getId(), userDetailsID);
        if(authentication.authenticate()) {
            return new ResponseEntity<>(userDetails, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
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
        User user;
        try {
            user = userRegistrationService.getUserById(userID);
            if(user == null) {
                throw new InvalidUserException();
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

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
        EditUserRequestParameters params = new EditUserRequestParameters(userID, details);
        Validator<EditUserRequestParameters> handler = new NullFieldsValidator<>();
        handler.link(new RequestUserValidator<>(userRegistrationService), new UserDetailsValidator<>(updateUserDetailsService));

        try {
            handler.handle(params);
        } catch (InputFormatException e) {
            if(e instanceof MalformedBodyException)
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            else if(e instanceof NotFoundException)
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            else if(e instanceof InvalidUserDetailsException)
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            else if(e instanceof UpdateDataException)
                return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>("User details updated successfully", HttpStatus.OK);
    }

    /**
     * POST /user/{userID}/makeAuthor - Give the user author privileges.
     *
     * @param userId ID of the user that makes the request.
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
     * GET /user/{userID}/search/interests
     * Searches a user by interests (favourite genres) and returns all matches
     * Matched users have at least all genres in the "interests" param in their favouriteGenres array
     * @param userID ID of the user that makes the request
     * @param interests List of genres to search by
     * @return Interests are null or contain null entries - Bad request 401
     *         User that makes the request does not exist - Not found 404
     *         Users cannot be fetched from the database - Internal server error 500
     *         No users match the query - Not found 404
     *         At least one user found that matches the interests - OK 200
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/user/{userID}/search/interests"
    )
    public ResponseEntity<List<User>> userSearchByInterests(@PathVariable(name="userID") Integer userID,
                                                        @RequestParam List<String> interests) {
        if(interests == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        for(String s : interests)
            if(s == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if(userRepository.findById(userID).isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch(Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        allUsers = allUsers.stream()
                .filter(user -> new HashSet<>(user.getUserDetails().getFavouriteGenres()).containsAll(interests))
                .collect(Collectors.toList());
        if(allUsers.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    /**
     * POST /user/{userID}/search/favoriteBooks
     * Searches a user by favoriteBooks and returns all matches
     * Matched users' favourite book has to be in the favoriteBooks array
     * @param userID ID of the user that makes the request
     * @param favoriteBooks Books to search by
     * @return favoriteBooks is null or contains null entries - Bad request 401
     *         User that makes the request does not exist - Not found 404
     *         Users cannot be retrieved from database - Internal server error 500
     *         No user matches the query - Not found 404
     *         At least one user matches the query - OK 200
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/user/{userID}/search/favoriteBooks",
            consumes = {"application/json"}
    )
    public ResponseEntity<List<User>> userSearchByBooks(@PathVariable(name="userID") Integer userID,
                                                        @RequestBody List<Book> favoriteBooks) {
        if (favoriteBooks == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        for(Book b : favoriteBooks)
            if(b == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if(userRepository.findById(userID).isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<Integer> ids = favoriteBooks.stream().map(Book::getId).collect(Collectors.toList());
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        allUsers = allUsers.stream()
                .filter(user -> ids.contains(user.getUserDetails().getFavouriteBookID()))
                .collect(Collectors.toList());
        if(allUsers.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    /**
     * POST user/{userID}/search/connections
     * Searches users by connections (other users actively followed)
     * Matched users have to follow all users in the connections array.
     * @param userID ID of the user that makes the request
     * @param connections Users to search by
     * @return connections array is null / contains null entries / email format invalid - Bad request 401
     *         User that makes the request doesn't exist - Not found 404
     *         Users cannot be retrieved from the database - Internal server error 500
     *         No user matches the query - Not found 404
     *         At least one user matches the query - OK 200
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/user/{userID}/search/connections",
            consumes = {"application/json"}
    )
    public ResponseEntity<List<User>> userSearchByConnections(@PathVariable(name="userID") Integer userID,
                                                              @RequestBody List<UserSearch> connections) {
        if(connections == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        for(UserSearch u : connections)
            if(u == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if(userRepository.findById(userID).isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<Email> emails = connections.stream().map(UserSearch::getEmail).collect(Collectors.toList());
        // if any email does not match the correct format => Bad request
        for(Email e : emails) {
            if(e == null)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch(Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // For some reasons, the "containsAll" method does not properly compare two Email instances.
        // I could not find the cause of this error, therefore I am getting the email as a String before comparing.
        allUsers = allUsers.stream()
                .filter(user ->
                        new HashSet<>(user
                                .getUserDetails()
                                .getFollowing()
                                .stream().map(follow -> follow.getEmail().getEmail()).collect(Collectors.toList()))
                            .containsAll(emails.stream().map(Email::getEmail).collect(Collectors.toList())))
                .collect(Collectors.toList());
        if(allUsers.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
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
