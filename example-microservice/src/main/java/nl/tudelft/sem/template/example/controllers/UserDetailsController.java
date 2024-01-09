package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.UserDetails.UserDetails;
import nl.tudelft.sem.template.example.domain.UserDetails.UserDetailsRepository;
import nl.tudelft.sem.template.example.domain.exceptions.InvalidUserException;
import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * This controller handles actions related to UserDetails
 */
@RestController
public class UserDetailsController {
    UserDetailsRepository userDetailsRepository;

    UserRepository userRepository;

    @Autowired
    public UserDetailsController(UserDetailsRepository userDetailsRepository,
                                 UserRepository userRepository) {
        this.userDetailsRepository = userDetailsRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping(
            method=RequestMethod.PUT,
            value="/user/{userID}/followUser/{followID}"
    )
    public ResponseEntity<String> followUser(@PathVariable("userID") int userID,
                                             @PathVariable("followID") int followID) {
        if(userID == followID)
            return new ResponseEntity<>("You cannot follow yourself!", HttpStatus.CONFLICT);

        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userID);
            if(optionalUser.isEmpty())
                throw new NoSuchElementException();
            user = optionalUser.get();
        } catch(NoSuchElementException e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        } catch(Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User toFollow;
        try {
            Optional<User> optionalUser = userRepository.findById(followID);
            if(optionalUser.isEmpty())
                throw new NoSuchElementException();
            toFollow = optionalUser.get();
        } catch(NoSuchElementException e) {
            return new ResponseEntity<>("User to follow could not be found", HttpStatus.NOT_FOUND);
        } catch(Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserDetails userDetails = user.getUserDetails();
        if(userDetails == null) {
            return new ResponseEntity<>("User details could not be found", HttpStatus.NOT_FOUND);
        }

        System.out.println(user.getUserDetails().getFollowing().toString());
        System.out.println(toFollow.getUserDetails().getFollowing().toString());
        try {
            if(userDetails.isFollowed(toFollow))
                throw new InvalidUserException();
        } catch (InvalidUserException e) {
            return new ResponseEntity<>("User is already being followed", HttpStatus.BAD_REQUEST);
        }

        userDetails.addFollowingItem(toFollow);

        try {
            userDetailsRepository.save(userDetails);
        } catch (Exception e) {
            return new ResponseEntity<>("Could not follow user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String message = "You are now following user with id:" + followID;
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @RequestMapping(
            method=RequestMethod.PUT,
            value="/user/{userID}/unfollowUser/{unfollowID}"
    )
    public ResponseEntity<String> unfollowUser(@PathVariable("userID") int userID,
                                               @PathVariable("unfollowID") int unfollowID) {
        if(userID == unfollowID)
            return new ResponseEntity<>("You cannot unfollow yourself!", HttpStatus.CONFLICT);

        User user;
        try {
            Optional<User> optionalUser = userRepository.findById(userID);
            if(optionalUser.isEmpty())
                throw new NoSuchElementException();
            user = optionalUser.get();
        } catch(NoSuchElementException e) {
            return new ResponseEntity<>("User could not be found", HttpStatus.NOT_FOUND);
        } catch(Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User toUnfollow;
        try {
            Optional<User> optionalUser = userRepository.findById(unfollowID);
            if(optionalUser.isEmpty())
                throw new NoSuchElementException();
            toUnfollow = optionalUser.get();
        } catch(NoSuchElementException e) {
            return new ResponseEntity<>("User to unfollow could not be found", HttpStatus.NOT_FOUND);
        } catch(Exception e) {
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        UserDetails userDetails = user.getUserDetails();
        if(userDetails == null) {
            return new ResponseEntity<>("User details could not be found", HttpStatus.NOT_FOUND);
        }

        if(!userDetails.isFollowed(toUnfollow))
            return new ResponseEntity<>("User is not being followed", HttpStatus.BAD_REQUEST);

        userDetails.removeFollowingItem(toUnfollow);

        try {
            userDetailsRepository.save(userDetails);
        } catch (Exception e) {
            return new ResponseEntity<>("Could not follow user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String message = "You are no longer following user with id:" + unfollowID;
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}