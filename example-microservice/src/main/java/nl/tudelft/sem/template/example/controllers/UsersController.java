package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.validation.Valid;
import nl.tudelft.sem.template.example.domain.exceptions.AlreadyHavePermissionsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidPasswordException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.RegistrationService;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.domain.user.VerificationService;

import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.models.UserPostRequest;
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

    RegistrationService registrationService;
    UserRepository userRepository;
    VerificationService verificationService;
    UpdateUserService updateUserService;

    @Autowired
    public UsersController(RegistrationService registrationService, UpdateUserService updateUserService,
                           UserRepository userRepository) {
        this.registrationService = registrationService;
        this.updateUserService = updateUserService;
        this.userRepository = userRepository;
        this.verificationService = new VerificationService();
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
        if (registrationService.getUserByEmail(email) != null) {
            return new ResponseEntity<>("User with email already exists", HttpStatus.CONFLICT);
        }

        User toAdd;
        //User details are present -> try to save it to the database
        try {
            toAdd = registrationService.registerUser(username, email, password);
        } catch (InvalidUserException e) {
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
     * POST /user/{userID}/makeAdmin : Gives the user admin privileges.
     *
     * @param userId ID of user that makes the request
     * @param password Password input by user
     * @return User with specific ID cannot be found (code 404)
     *         Something went wrong while retrieving the user (code 500)
     *         Password is null or invalid (code 400)
     *         User is already an admin (code 409)
     *         User has succesfully become an admin (code 500)
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
            Boolean result = verificationService.verifyAdminRequest(user, password);
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
     * GET /user/{userID} : Fetch user information
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
            user = registrationService.getUserById(userID);
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
     * PUT /user/{userID}/changePassword : Change user password
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
        } catch (Exception e) {
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

}