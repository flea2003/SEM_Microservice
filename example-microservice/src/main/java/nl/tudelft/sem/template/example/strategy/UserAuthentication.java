package nl.tudelft.sem.template.example.strategy;

/*
This class implements the Strategy Design Pattern
Although in this scenario the logic is pretty straightforward
It would make sense if we would add extra security to out Microservice
*/
public class UserAuthentication implements Authentication{

    private Integer userId;
    private Integer requestId;

    public UserAuthentication(Integer userId, Integer requestId) {
        this.userId = userId;
        this.requestId = requestId;
    }

    @Override
    public boolean authenticate() {
        return userId.equals(requestId);
    }
}
