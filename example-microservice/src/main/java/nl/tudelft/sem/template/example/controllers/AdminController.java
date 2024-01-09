package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRegistrationService;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    UserRegistrationService userRegistrationService;
    UserDetailsRegistrationService userDetailsRegistrationService;
    UserRepository userRepository;
    UserDetailsRepository userDetailsRepository;
    VerificationService verificationService;
    UpdateUserService updateUserService;

    /**
     * Constructor for the admin controller.
     */
    @Autowired
    public AdminController(UserRegistrationService userRegistrationService, UpdateUserService updateUserService,
                           UserRepository userRepository, UserDetailsRepository userDetailsRepository,
                           UserDetailsRegistrationService userDetailsRegistrationService) {
        this.userRegistrationService = userRegistrationService;
        this.updateUserService = updateUserService;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.verificationService = new VerificationService();
        this.userDetailsRegistrationService = userDetailsRegistrationService;
    }

    /**
     * PUT /admin/{adminID}/banUser/{userID} : Bans the account of a user.
     *
     * @param adminID Numeric ID of the admin that makes the request
     * @param userID Numeric ID of the user to ban
     * @return 200: User banned successfully
     *         401: User does not have admin privileges
     *         404: User could not be found
     *         500: User account could not be modified
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/admin/{adminID}/banUser/{userID}"
    )
    public ResponseEntity<String> banUser(
            @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID) {
        //First find the two users
        if (adminID == null || userID == null) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }
        User adminUser;
        User toModify;
        try {
            adminUser = userRegistrationService.getUserById(adminID);
            toModify = userRegistrationService.getUserById(userID);
            if (adminUser == null || toModify == null) {
                throw new RuntimeException("Users not found");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        //Check if user is admin
        if (!adminUser.getIsAdmin()) {
            return new ResponseEntity<>("User does not have admin privileges", HttpStatus.UNAUTHORIZED);
        }

        //Ban user
        try {
            toModify.setIsBanned(true);
            userRepository.save(toModify);
            return new ResponseEntity<>("User banned successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("User account could not be modified", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /admin/{adminID}/unbanUser/{userID} : Unbans the account of a user.
     *
     * @param adminID Numeric ID of the admin that makes the request
     * @param userID Numeric ID of the user to unban
     * @return 200: User unbanned successfully
     *         401: User does not have admin privileges
     *         404: User could not be found
     *         500: User account could not be modified
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/admin/{adminID}/unbanUser/{userID}"
    )
    public ResponseEntity<String> unbanUser(
            @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID) {
        //First find the two users
        if (adminID == null || userID == null) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }
        User adminUser;
        User toModify;
        try {
            adminUser = userRegistrationService.getUserById(adminID);
            toModify = userRegistrationService.getUserById(userID);
            if (adminUser == null || toModify == null) {
                throw new RuntimeException("Users not found");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        //Check if user is admin
        if (!adminUser.getIsAdmin()) {
            return new ResponseEntity<>("User does not have admin privileges", HttpStatus.UNAUTHORIZED);
        }

        //Ban user
        try {
            toModify.setIsBanned(false);
            userRepository.save(toModify);
            return new ResponseEntity<>("User unbanned successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("User account could not be modified", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /admin/{adminID}/deleteUser/{userID} : Deletes the account of a user from the user repository.
     *
     * @param adminID Numeric ID of the admin that makes the request
     * @param userID Numeric ID of the user to delete
     * @return 200: User deleted successfully
     *         401: User does not have admin privileges
     *         404: User could not be found
     *         500: User account could not be deleted
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/admin/{adminID}/deleteUser/{userID}"
    )
    public ResponseEntity<String> deleteUser(
            @Parameter(name = "adminID", required = true, in = ParameterIn.PATH) @PathVariable("adminID") Integer adminID,
            @Parameter(name = "userID", required = true, in = ParameterIn.PATH) @PathVariable("userID") Integer userID) {
        //First find the two users
        if (adminID == null || userID == null) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }
        User adminUser;
        User toDelete;
        try {
            adminUser = userRegistrationService.getUserById(adminID);
            toDelete = userRegistrationService.getUserById(userID);
            if (adminUser == null || toDelete == null) {
                throw new RuntimeException("Users not found");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }

        //Check if user is admin
        if (!adminUser.getIsAdmin()) {
            return new ResponseEntity<>("User does not have admin privileges", HttpStatus.UNAUTHORIZED);
        }

        //Delete user
        try {
            userRepository.delete(toDelete);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("User account could not be deleted", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
