package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.Valid;

import nl.tudelft.sem.template.example.domain.user.*;
import nl.tudelft.sem.template.example.models.UserPostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Users controller.
 * <p>
 * This controller handles actions related to users.
 * </p>
 */
@RestController
public class UsersController {

    RegistrationService registrationService;
    UpdateUserService updateUserService;

    @Autowired
    public UsersController(RegistrationService registrationService, UpdateUserService updateUserService) {
        this.registrationService = registrationService;
        this.updateUserService = updateUserService;
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

    @GetMapping(value = "/user/{userID}")
    public ResponseEntity<User> userGetUser(
            @Parameter(name = "userID", description = "Numeric ID of the user that makes the request", required = true)
            @PathVariable("userID") Integer userID) {

        User user = registrationService.getUserById(userID);

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * PUT /user/{userID}/changePassword : Change user password
     *
     * @param userID Numeric ID of the user that makes the request (required)
     * @param body  (required)
     * @return Password changed successfully (status code 200)
     *         or User not logged in (status code 401)
     *         or Password could not be changed (status code 500)
     */
    @PutMapping(value = "/user/{userID}/changePassword")
    public ResponseEntity<String> userUserIDChangePasswordPut(
            @Parameter(name = "userID", required = true) @PathVariable("userID") Integer userID,
            @Parameter(name = "body", description = "Desired Password", required = true) @Valid @RequestBody String body
    ) {
        if (userID == null || body.isEmpty()) {
            return new ResponseEntity<>("Request body is malformed", HttpStatus.BAD_REQUEST);
        }
        User user = registrationService.getUserById(userID);

        if (user == null) {
            return new ResponseEntity<>("User couldn't found",HttpStatus.NOT_FOUND);
        }

        HashedPassword hashedPassword = PasswordHashingService.hash(body);

        User updatedUser;
        try {
            updatedUser = updateUserService.updatePassword(userID, hashedPassword);
        } catch (Exception e) {
            return new ResponseEntity<>("Database insertion failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(updatedUser.getPassword().equals(hashedPassword)){
            return new ResponseEntity<>("Your password has been changed successfully.", HttpStatus.OK);
        }
        return new ResponseEntity<>("We couldn't change your password.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
