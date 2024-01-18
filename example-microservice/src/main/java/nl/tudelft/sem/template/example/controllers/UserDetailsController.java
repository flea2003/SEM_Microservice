package nl.tudelft.sem.template.example.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.NoSuchElementException;
import java.util.Optional;
import nl.tudelft.sem.template.example.domain.UserDetails.UpdateUserDetailsService;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserDetailsException;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import nl.tudelft.sem.template.example.strategy.Authentication;
import nl.tudelft.sem.template.example.strategy.UserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller handles actions related to UserDetails.
 */
@RestController
public class UserDetailsController {
    UserDetailsRepository userDetailsRepository;

    UserRepository userRepository;
    UpdateUserDetailsService updateUserDetailsService;

    /**
     * Constructor for this controller.
     */
    @Autowired
    public UserDetailsController(UserDetailsRepository userDetailsRepository,
                                 UserRepository userRepository,
                                 UpdateUserDetailsService updateUserDetailsService) {
        this.userDetailsRepository = userDetailsRepository;
        this.userRepository = userRepository;
        this.updateUserDetailsService = updateUserDetailsService;
    }

    /**
     * Endpoint for following a user.
     *
     * @param userId Id of performing user
     * @param followId Id of user to follow
     * @return 200 OK if the request was successful, or an error message of what went wrong
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/user/{userID}/followUser/{followID}"
    )
    public ResponseEntity<String> followUser(@PathVariable("userID") int userId,
                                             @PathVariable("followID") int followId) {
        if (userId == followId) {
            return new ResponseEntity<>("You cannot follow yourself!", HttpStatus.CONFLICT);
        }

        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User toFollow;
        try {
            Optional<User> optionalUser = userRepository.findById(followId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            toFollow = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User to follow could not be found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserDetails userDetails = user.getUserDetails();
        if (userDetails == null) {
            return new ResponseEntity<>("User details could not be found", HttpStatus.NOT_FOUND);
        }

        try {
            if (userDetails.isFollowed(toFollow)) {
                throw new InvalidUserException();
            }
        } catch (InvalidUserException e) {
            return new ResponseEntity<>("User is already being followed", HttpStatus.BAD_REQUEST);
        }

        userDetails.addFollowingItem(toFollow);

        try {
            userDetailsRepository.save(userDetails);
        } catch (Exception e) {
            return new ResponseEntity<>("Could not follow user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String message = "You are now following user with id:" + followId;
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * Endpoint for unfollowing a user.
     *
     * @param userId Id of performing user
     * @param unfollowId Id of user to unfollow
     * @return 200 OK if the request was successful, or an error message indicating what went wrong.
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/user/{userID}/unfollowUser/{unfollowID}"
    )
    public ResponseEntity<String> unfollowUser(@PathVariable("userID") int userId,
                                               @PathVariable("unfollowID") int unfollowId) {
        if (userId == unfollowId) {
            return new ResponseEntity<>("You cannot unfollow yourself!", HttpStatus.CONFLICT);
        }

        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            user = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User toUnfollow;
        try {
            Optional<User> optionalUser = userRepository.findById(unfollowId);
            if (optionalUser.isEmpty()) {
                throw new NoSuchElementException();
            }
            toUnfollow = optionalUser.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User to unfollow could not be found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserDetails userDetails = user.getUserDetails();
        if (userDetails == null) {
            return new ResponseEntity<>("User details could not be found", HttpStatus.NOT_FOUND);
        }

        if (!userDetails.isFollowed(toUnfollow)) {
            return new ResponseEntity<>("User is not being followed", HttpStatus.BAD_REQUEST);
        }

        userDetails.removeFollowingItem(toUnfollow);

        try {
            userDetailsRepository.save(userDetails);
        } catch (Exception e) {
            return new ResponseEntity<>("Could not follow user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String message = "You are no longer following user with id:" + unfollowId;
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    /**
     * GET user/{userID}/userDetails/{userDetails}.
     *
     * @param userId        - Numeric ID of the user that makes the request
     * @param userDetailsId - Numeric ID of the userDetails that are requested
     * @return Unauthorised access to details (status code 401)
     *      Details not found (status code 404)
     *      User details cannot be accessed (status code 500)
     *      User details fetched successfully (status code 200) + userDetails
     */
    //@GetMapping(value = "/user/{userID}/userDetails/{userDetailsID}")
    public ResponseEntity<UserDetails> getUserDetails(
        @Parameter(name = "userID", description = "Numeric ID of the user that makes the request",
            required = true, in = ParameterIn.PATH)
        @PathVariable("userID") Integer userId,
        @Parameter(name = "userDetailsID", description = "ID of the details that are requested",
            required = true, in = ParameterIn.PATH)
        @PathVariable("userDetailsID") Integer userDetailsId
    ) {
        if (userId == null || userDetailsId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                throw new InvalidUserException();
            }
            user = optionalUser.get();
        } catch (InvalidUserException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails;
        try {
            Optional<UserDetails> optionalUserDetails = userDetailsRepository.findById(userDetailsId);
            if (optionalUserDetails.isEmpty()) {
                throw new NoSuchElementException();
            }
            if (optionalUserDetails.get().getId() < 0) {
                throw new IllegalArgumentException();
            }
            userDetails = optionalUserDetails.get();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Authentication authentication = new UserAuthentication(user.getUserDetails().getId(), userDetailsId);
        if (authentication.authenticate()) {
            return new ResponseEntity<>(userDetails, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }


    /**
     * PUT /user/{userID}/editUser : Edit the user's details.
     *
     * @param userId Numeric ID of the user that makes the request (required)
     * @return User details updated successfully (status code 200)
     *      or Unauthorised changes to the user (status code 401)
     *      or User could not be found (status code 404)
     *      or User could not be updated or new data is invalid (status code 500)
     */
    @PutMapping(value = "/user/{userID}/editUser")
    public ResponseEntity<String> editUserDetails(
        @Parameter(name = "userID", description = "Numeric ID of the user that makes the request",
            required = true, in = ParameterIn.PATH)
        @PathVariable("userID") Integer userId,
        @RequestBody UserDetails details) {
        if (userId == null || details == null) {
            return new ResponseEntity<>("Request is malformed", HttpStatus.BAD_REQUEST);
        }
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        }
        User user = optionalUser.get();
        try {
            updateUserDetailsService.updateUserDetails(user.getId(), details);
        } catch (InvalidUserDetailsException e) {
            return new ResponseEntity<>("User could not be updated or new data is invalid",
                HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unauthorised changes to the user", HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>("User details updated successfully", HttpStatus.OK);
    }
}