package nl.tudelft.sem.template.example.domain.analytics;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {

    private AnalyticsService service;

    @BeforeEach
    void setup() {
        UserActionRepository repository = new TestUserActionRepository();
        service = new AnalyticsService(repository);
    }

    @Test
    void countLoginsTest() {
        User user = new User();
        service.recordLogin(user);
        service.recordLogin(user);
        service.recordLogin(user);
        service.recordLogin(user);

        Analytics analytics = service.compileAnalytics();
        assertEquals(4, analytics.getNoLogins());
    }

    @Test
    void popularGenresTest() {
        User user = new User();
        service.recordGenreInteraction(user, "historical fiction");
        service.recordGenreInteraction(user, "horror");
        service.recordGenreInteraction(user, "horror");
        service.recordGenreInteraction(user, "horror");
        service.recordGenreInteraction(user, "fantasy");
        service.recordGenreInteraction(user, "fantasy");
        service.recordGenreInteraction(user, "non-fiction");
        service.recordGenreInteraction(user, "non-fiction");

        Analytics analytics = service.compileAnalytics();
        assertEquals(3, analytics.getPopularGenres().size());
        System.out.println(analytics.getPopularGenres());
        assertTrue(analytics.getPopularGenres().contains("horror"));
        assertTrue(analytics.getPopularGenres().contains("non-fiction"));
        assertTrue(analytics.getPopularGenres().contains("fantasy"));
        assertFalse(analytics.getPopularGenres().contains("historical fiction"));
    }

}
