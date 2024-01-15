package nl.tudelft.sem.template.example.strategy;

import nl.tudelft.sem.template.example.domain.user.User;
import nl.tudelft.sem.template.example.domain.user.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/*
This class implements the Strategy Design Pattern
Although in this scenario the logic is pretty straightforward
It would make sense if we would add extra security to out Microservice
*/
public class AdminAuthentication implements Authentication{

    private UserRegistrationService userRegistrationService;
    private Integer adminID;

    /**
     * Instantiates a new AdminAuthentication strategy
     * @param adminID - the ID of the person that we want to check for
     * @param userRegistrationService - the registration service where we would make the query
     */
    public AdminAuthentication(Integer adminID, UserRegistrationService userRegistrationService) {
        this.adminID = adminID;
        this.userRegistrationService = userRegistrationService;
    }

    /**
     * Tries to authenticate the user
     * @return whether a user was authenticated or not
     */
    @Override
    public boolean authenticate() {
        User admin = userRegistrationService.getUserById(adminID);
        if(admin == null) return false;
        else {
            return admin.getIsAdmin();
        }
    }
}
