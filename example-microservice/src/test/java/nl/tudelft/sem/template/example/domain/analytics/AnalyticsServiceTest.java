package nl.tudelft.sem.template.example.domain.analytics;

import nl.tudelft.sem.template.example.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        UserAction res = service.recordLogin(user);
        service.recordLogin(user);
        service.recordLogin(user);
        service.recordLogin(user);

        Analytics analytics = service.compileAnalytics();
        assertEquals(4, analytics.getNoLogins());
        assertNotNull(res);
    }

    @Test
    void popularGenresTest() {
        User user = new User();
        UserAction res = service.recordGenreInteraction(user, "historical fiction");
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
        assertNotNull(res);
    }

    @Test
    void testGetById(){
        UserActionRepository repositoryMock = Mockito.mock(UserActionRepository.class);
        AnalyticsService sut = new AnalyticsService(repositoryMock);

        UserAction userAction = new UserAction();
        userAction.setId(10);
        when(repositoryMock.findById(10)).thenReturn(Optional.of(userAction));

        assertEquals(userAction, sut.getActionById(10));
    }

}
