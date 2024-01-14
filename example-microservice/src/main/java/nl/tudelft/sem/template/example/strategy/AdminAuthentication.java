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

    public AdminAuthentication(Integer adminID, UserRegistrationService userRegistrationService) {
        this.adminID = adminID;
        this.userRegistrationService = userRegistrationService;
    }

    @Override
    public boolean authenticate() {
        User admin = userRegistrationService.getUserById(adminID);
        if(admin == null) return false;
        else {
            return admin.getIsAdmin();
        }
    }
}
