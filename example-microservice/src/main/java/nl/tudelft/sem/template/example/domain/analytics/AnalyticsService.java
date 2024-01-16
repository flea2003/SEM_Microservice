package nl.tudelft.sem.template.example.domain.analytics;

import nl.tudelft.sem.template.example.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A service class for logging analytics, such as popular genres and user login activity
 */
@Service
public class AnalyticsService {

    private final transient UserActionRepository actionRepository;

    /**
     * Instantiates a new analytics service
     *
     * @param actionRepository the repository of user actions to use for this service
     */
    public AnalyticsService(UserActionRepository actionRepository) {
        assert actionRepository != null : "Action repository cannot be null";
        this.actionRepository = actionRepository;
    }

    /**
     * Records a login by a user in the user action table
     *
     * @param user the user that logged in
     * @return the user action object representing the login
     */
    public UserAction recordLogin(User user) {
        UserAction action = new UserAction(user, "login");
        this.actionRepository.save(action);
        return action;
    }

    /**
     * Records a genre interaction by a user in the user action table
     *
     * @param user the user which interacted with a genre
     * @param genre the genre which was interacted with
     * @return the user action object representing this interaction
     */
    public UserAction recordGenreInteraction(User user, String genre) {
        UserAction action = new UserAction(user, genre);
        this.actionRepository.save(action);
        return action;
    }

    /**
     * Fetches a user action object given its ID
     *
     * @param actionID the ID of the action object
     * @return the action object itself
     */
    public UserAction getActionByID(int actionID) {
        Optional<UserAction> action = this.actionRepository.findById(actionID);
        return action.orElse(null);
    }

    /**
     * Compiles the user action data and creates an analytics object based on the data
     *
     * @return an analytics object given the recorded user activity
     */
    public Analytics compileAnalytics() {
        int logins = (int) this.actionRepository.countByType("login");
        List<Object[]> results = this.actionRepository.getActionsByTypeFrequency();
        results.removeIf(result -> result[0].equals("login"));

        List<String> popularGenres = new ArrayList<>();
        for(int i = 0; i < Math.min(3, results.size()); ++i)
            popularGenres.add((String) results.get(i)[0]);

        // ID field is just here to adhere to the API spec
        return new Analytics(0, popularGenres, logins);
    }

}
