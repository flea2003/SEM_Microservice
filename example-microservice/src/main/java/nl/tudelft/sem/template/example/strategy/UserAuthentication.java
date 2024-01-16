package nl.tudelft.sem.template.example.strategy;

/*
This class implements the Strategy Design Pattern
Although in this scenario the logic is pretty straightforward
It would make sense if we would add extra security to out Microservice
*/
public class UserAuthentication implements Authentication{

    private Integer userId;
    private Integer requestId;

    /**
     * Instantiates a new UserAuthentication strategy
     * @param userId - the ID of the user who makes the request
     * @param requestId - the ID in the request
     */
    public UserAuthentication(Integer userId, Integer requestId) {
        this.userId = userId;
        this.requestId = requestId;
    }

    /**
     * Tries to authenticate an user
     * @return whether the user got authenticated or not
     */
    @Override
    public boolean authenticate() {
        return userId.equals(requestId);
    }
}
