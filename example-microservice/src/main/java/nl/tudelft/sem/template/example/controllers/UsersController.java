package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettings;
import nl.tudelft.sem.template.example.domain.accountsettings.AccountSettingsRegistrationService;
import nl.tudelft.sem.template.example.domain.analytics.AnalyticsService;
import nl.tudelft.sem.template.example.domain.book.Book;
import nl.tudelft.sem.template.example.domain.exceptions.AlreadyExistsException;
import nl.tudelft.sem.template.example.domain.exceptions.AlreadyHavePermissionsException;
import nl.tudelft.sem.template.example.domain.exceptions.InputFormatException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidEmailException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidPasswordException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUsernameException;
import nl.tudelft.sem.template.example.domain.exceptions.MalformedBodyException;
import nl.tudelft.sem.template.example.domain.user.Email;
import nl.tudelft.sem.template.example.domain.user.HashedPassword;
import nl.tudelft.sem.template.example.domain.user.PasswordHashingService;
import nl.tudelft.sem.template.example.domain.user.UpdateUserService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.domain.user.VerificationService;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetails;
import nl.tudelft.sem.template.example.domain.userdetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.handlers.Validator;
import nl.tudelft.sem.template.example.handlers.userCreation.EmailFormatValidator;
import nl.tudelft.sem.template.example.handlers.userCreation.NoSameEmailUserValidator;
import nl.tudelft.sem.template.example.handlers.userCreation.NullOrEmptyFieldsValidator;
import nl.tudelft.sem.template.example.handlers.userCreation.UsernameFormatValidator;
import nl.tudelft.sem.template.example.models.DocumentConversionRequest;
import nl.tudelft.sem.template.example.models.LoginPostRequest;
import nl.tudelft.sem.template.example.models.UserPostRequest;
import nl.tudelft.sem.template.example.models.UserSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Users controller.
 * <p>
 * This controller handles actions related to users.
 * </p>
 */
@RestController
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals", "PMD.AvoidInstanceofChecksInCatchClause"})
public class UsersController {
    transient UserRepository userRepository;
    transient UserRegistrationService userRegistrationService;
    transient UserDetailsRegistrationService userDetailsRegistrationService;
    transient AccountSettingsRegistrationService accountSettingsRegistrationService;
    transient VerificationService verificationService;
    transient UpdateUserService updateUserService;
    transient AnalyticsService analyticsService;
    transient AccountSettingsController accountSettingsController;
    transient UserDetailsController userDetailsController;

    /**
     * Constructor for the users controller.
     */
    @Autowired
    public UsersController(UserRepository userRepository,
                           UserRegistrationService userRegistrationService,
                           UpdateUserService updateUserService,
                           UserDetailsRegistrationService userDetailsRegistrationService,
                           AccountSettingsRegistrationService accountSettingsRegistrationService,
                           AnalyticsService analyticsService,
                           AccountSettingsController accountSettingsController,
                           UserDetailsController userDetailsController) {
        this.userRepository = userRepository;
        this.userRegistrationService = userRegistrationService;
        this.updateUserService = updateUserService;
        this.verificationService = new VerificationService();
        this.userDetailsRegistrationService = userDetailsRegistrationService;
        this.accountSettingsRegistrationService = accountSettingsRegistrationService;
        this.analyticsService = analyticsService;
        this.accountSettingsController = accountSettingsController;
        this.userDetailsController = userDetailsController;
    }

    /**
     * Utility function for the checking below.
     *
     * @param field A list of objects to go through and check for nulls
     * @return whether the list contains nulls or not
     */
    public boolean arrayHasNullsUtility(List<?> field) {
        for (Object o : field) {
            if (o == null) {
                return true;
            }
        }
        return false;
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
     * POST /user : Creates a new user and logs them in.
     *
     * @param userPostRequest (required)
     * @return User was created and logged in successfully (status code 200)
     *      or Request body is malformed (status code 400)
     *      or User with username/email already exists (status code 409)
     *      or Internal registration failure (status code 500)
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/user",
        consumes = {"application/json"}
    )
    public ResponseEntity<String> userPost(@Parameter(name = "UserPostRequest", required = true) @Valid
                                           @RequestBody UserPostRequest userPostRequest) {
        // Create Chain
        Validator<UserPostRequest> handler = new NullOrEmptyFieldsValidator<>();
        //handler.setNextOperation(new EmailFormatValidator<>());
        handler.link(new EmailFormatValidator<>(), new UsernameFormatValidator<>(),
            new NoSameEmailUserValidator<>(userRegistrationService));
        // Handle exceptions
        try {
            handler.handle(userPostRequest);
        } catch (InputFormatException e) {
            if (e instanceof MalformedBodyException || e instanceof InvalidEmailException
                || e instanceof InvalidUsernameException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } else if (e instanceof AlreadyExistsException) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
            }
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

        // Record a login in the analytics service
        try {
            analyticsService.recordLogin(toAdd);
        } catch (Exception ex) {
            return new ResponseEntity<>("Database update failed", HttpStatus.INTERNAL_SERVER_ERROR);
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
     * @param loginPostRequest (required)
     * @return User was logged in successfully (status code 200)
     *      or Request body is malformed (status code 400)
     *      or Invalid username or password (status code 401)
     *      or Internal login failure (status code 500)
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/login",
        consumes = {"application/json"}
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
                try {
                    analyticsService.recordLogin(u);
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }

                return new ResponseEntity<>(u, HttpStatus.OK);
            }
        }

        //Password was not correct
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /**
     * POST /user/{userID}/makeAdmin : Gives the user admin privileges.
     *
     * @param userId   ID of user that makes the request
     * @param password Password input by user
     * @return User with specific ID cannot be found (code 404)
     *      Something went wrong while retrieving the user (code 500)
     *      Password is null or invalid (code 401)
     *      User is already an admin (code 409)
     *      User has successfully become an admin (code 200)
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/user/{userID}/makeAdmin",
        consumes = {"text/plain"}
    )
    public ResponseEntity<String> makeAdmin(@PathVariable(name = "userID") int userId,
                                            @RequestBody String password) {
        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>((String) checkUserIdResult.getBody(), checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();

        try {
            verificationService.verifyAdminRequest(user, password);
        } catch (InvalidPasswordException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (AlreadyHavePermissionsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }

        user.setIsAdmin(true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be updated", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String message = "User with ID:" + userId + " is now an admin";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * POST /user/{userID}/makeAuthor - Give the user author privileges.
     *
     * @param userId   ID of the user that makes the request.
     * @param document document object provided by the user for verification.
     * @return Request body is malformed (code 400)
     *      User with provided ID could not be found (code 404)
     *      Database error (code 500)
     *      User is already an author (code 409)
     *      Document is invalid (code 401)
     *      User has successfully become an author (code 200)
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/user/{userID}/makeAuthor",
        consumes = {"application/json"}
    )
    public ResponseEntity<String> makeAuthor(@PathVariable(name = "userID") int userId,
                                             @RequestBody DocumentConversionRequest document) {
        if (document == null || document.getDocumentId() == null) {
            return new ResponseEntity<>("Request body is malformed", HttpStatus.BAD_REQUEST);
        }
        int documentId = document.getDocumentId();

        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>((String) checkUserIdResult.getBody(), checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();

        try {
            verificationService.verifyAuthorRequest(user, documentId);
        } catch (AlreadyHavePermissionsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (InvalidPasswordException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        user.setIsAuthor(true);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be saved", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String message = "User with ID:" + userId + " is now an author";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * GET /user/{userID} : Fetch user information.
     *
     * @param userId Numeric ID of the user that makes the request (required)
     * @return User data fetched successfully (status code 200)
     *      or User is not authenticated (status code 401)
     *      or Internal server error (status code 500)
     */
    @GetMapping(value = "/user/{userID}")
    public ResponseEntity<User> userGetUser(
        @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true)
        @PathVariable("userID") Integer userId) {

        User user;
        try {
            user = userRegistrationService.getUserById(userId);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return user != null ? new ResponseEntity<>(user, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * PUT /user/{userID}/changePassword : Change user password.
     *
     * @param userId Numeric ID of the user that makes the request (required)
     * @param body   The desired password (required)
     * @return Password changed successfully (status code 200)
     *      or Request body is malformed (status code 401)
     *      or Password could not be changed (status code 500)
     */
    @PutMapping(value = "/user/{userID}/changePassword")
    public ResponseEntity<String> userChangePassword(
        @Parameter(name = "userID", required = true) @PathVariable("userID") Integer userId,
        @Parameter(name = "body", description = "Desired Password", required = true) @Valid @RequestBody String body
    ) {
        if (userId == null || body.isEmpty()) {
            return new ResponseEntity<>("Request body is malformed", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>((String) checkUserIdResult.getBody(), checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();

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
     * DELETE /user/{userID}/delete/{} : User wants to delete their account by own choice.
     *
     * @param userId Numeric ID of the user that makes the request (required)
     * @return User account deletion successful (status code 200)
     *      or User not logged in (status code 401)
     *      or User not found (status code 404)
     *      or User account could not be deleted (status code 500)
     */
    @Operation(
        operationId = "userUserIDDelete",
        summary = "User wants to delete their account by own choice.",
        tags = {"User Operations"},
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
    public ResponseEntity<Void> userUserIdDelete(
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
            userRepository.delete(user);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * GET /user/{userID}/search/interests.
     * Searches a user by interests (favourite genres) and returns all matches
     * Matched users have at least all genres in the "interests" param in their favouriteGenres array
     *
     * @param userId    ID of the user that makes the request
     * @param interests List of genres to search by
     * @return Interests are null or contain null entries - Bad request 401
     *      User that makes the request does not exist - Not found 404
     *      Users cannot be fetched from the database - Internal server error 500
     *      No users match the query - Not found 404
     *      At least one user found that matches the interests - OK 200
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/user/{userID}/search/interests"
    )
    public ResponseEntity<List<User>> userSearchByInterests(@PathVariable(name = "userID") Integer userId,
                                                            @RequestParam List<String> interests) {
        if (interests == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (arrayHasNullsUtility(interests)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>(checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Record genre interactions
        for (String interest : interests) {
            try {
                analyticsService.recordGenreInteraction(user, interest);
            } catch (Exception ex) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        allUsers = allUsers.stream()
            .filter(currentUser -> new HashSet<>(currentUser.getUserDetails().getFavouriteGenres()).containsAll(interests))
            .collect(Collectors.toList());
        if (allUsers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    /**
     * POST /user/{userID}/search/favoriteBooks.
     * Searches a user by favoriteBooks and returns all matches
     * Matched users' favourite book has to be in the favoriteBooks array
     *
     * @param userId        ID of the user that makes the request
     * @param favoriteBooks Books to search by
     * @return favoriteBooks is null or contains null entries - Bad request 401
     *      User that makes the request does not exist - Not found 404
     *      Users cannot be retrieved from database - Internal server error 500
     *      No user matches the query - Not found 404
     *      At least one user matches the query - OK 200
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/user/{userID}/search/favoriteBooks",
        consumes = {"application/json"}
    )
    public ResponseEntity<List<User>> userSearchByBooks(@PathVariable(name = "userID") Integer userId,
                                                        @RequestBody List<Book> favoriteBooks) {
        if (favoriteBooks == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (arrayHasNullsUtility(favoriteBooks)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>(checkUserIdResult.getStatusCode());
        }
        User user = (User) checkUserIdResult.getBody();
        List<Integer> ids = favoriteBooks.stream().map(Book::getId).collect(Collectors.toList());
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Record genre interactions
        for (Book book : favoriteBooks) {
            for (String genre : book.getGenres()) {
                try {
                    analyticsService.recordGenreInteraction(user, genre);
                } catch (Exception ex) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }

        allUsers = allUsers.stream()
            .filter(currentUser -> ids.contains(currentUser.getUserDetails().getFavouriteBookID()))
            .collect(Collectors.toList());
        if (allUsers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    /**
     * POST user/{userID}/search/connections
     * Searches users by connections (other users actively followed)
     * Matched users have to follow all users in the connections array.
     *
     * @param userId      ID of the user that makes the request
     * @param connections Users to search by
     * @return connections array is null / contains null entries / email format invalid - Bad request 401
     *      User that makes the request doesn't exist - Not found 404
     *      Users cannot be retrieved from the database - Internal server error 500
     *      No user matches the query - Not found 404
     *      At least one user matches the query - OK 200
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/user/{userID}/search/connections",
        consumes = {"application/json"}
    )
    public ResponseEntity<List<User>> userSearchByConnections(@PathVariable(name = "userID") Integer userId,
                                                              @RequestBody List<UserSearch> connections) {
        if (connections == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (arrayHasNullsUtility(connections)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>(checkUserIdResult.getStatusCode());
        }
        List<Email> emails = connections.stream().map(UserSearch::getEmail).collect(Collectors.toList());
        // if any email does not match the correct format => Bad request
        if (arrayHasNullsUtility(emails)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch (Exception e) {
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
        if (allUsers.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    /**
     * GET /user/{userID}/search/{name} : Search for users based on a query.
     *
     * @param userId The id of the searcher user (required)
     * @param name   The search query (required)
     * @return Users matching the search query (status code 200)
     *      or No users found (status code 404)
     *      or Internal server error (status code 500)
     */
    @GetMapping(value = "/user/{userID}/search/{name}")
    public ResponseEntity<List<User>> userSearch(
        @Parameter(name = "userID", required = true) @PathVariable("userID") Integer userId,
        @Parameter(name = "name", description = "Name of the searched user", required = true)
        @PathVariable String name) {

        if (name == null || name.isEmpty() || userId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>(checkUserIdResult.getStatusCode());
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
     * GET user/{userID}/userDetails/{anyID}.
     * This serves as a decision function between the 2 endpoints which have the same string path, the request can't
     * distinguish based on a path parameter, because they are both Integers.
     * First looks whether the ID corresponds to a UserDetails instance or an AccountSettings one, then calls the
     * relevant endpoint.
     *
     * @param userId Numeric ID of the user that makes the request
     * @param anyId  ID of either the account settings or user details that are requested
     * @return Unauthorised access (status code 401)
     *      Neither UserDetails nor AccountSettings found (status code 404)
     *      User details or account settings fetched successfully (status code 200) and relevant entity
     */
    @GetMapping(value = "/user/{userID}/userDetails/{anyID}")
    public ResponseEntity<? extends Object> getUserDetailsOrAccountSettings(
        @Parameter(name = "userID", description = "Numeric ID of the user that makes the request",
            required = true, in = ParameterIn.PATH)
        @PathVariable("userID") Integer userId,
        @Parameter(name = "anyID", description = "ID of either the account settings or user details that are requested",
            required = true, in = ParameterIn.PATH)
        @PathVariable("anyID") Integer anyId
    ) {
        // basic checking beforehand
        if (userId == null || anyId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ResponseEntity<?> checkUserIdResult =
            existenceCheckingWithCustomMessages(userId, "User with that ID could not be found", "Something went wrong");
        if (checkUserIdResult.getBody() instanceof String) {
            return new ResponseEntity<>((String) checkUserIdResult.getBody(), checkUserIdResult.getStatusCode());
        }

        Optional<? extends Object> optional =
            userDetailsRegistrationService.findById(anyId); // first look whether it was a UserDetails request
        if (optional.isEmpty()) {
            optional = accountSettingsRegistrationService.findById(anyId);
            if (optional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else { // it was AccountSettings
                return accountSettingsController.getAccountSettings(userId, anyId);
            }
        } else { // it was UserDetails
            return userDetailsController.getUserDetails(userId, anyId);
        }
    }
}
