package nl.tudelft.sem.template.example.domain.analytics;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AnalyticsTest {

    @Test
    void testCreateAnalytics() {
        Analytics analytics = new Analytics(5, Arrays.asList("Historical Fiction", "Fantasy"), 20);
        assertEquals(2, analytics.getPopularGenres().size());
        assertEquals("Historical Fiction", analytics.getPopularGenres().get(0));
        assertEquals("Fantasy", analytics.getPopularGenres().get(1));
        assertEquals(20, analytics.getNoLogins());
    }

    @Test
    void testAnalyticsEquals() {
        Analytics analytics0 = new Analytics(0, Arrays.asList("Historical Fiction", "Fantasy"), 20);
        Analytics analytics1 = new Analytics(0, Arrays.asList("Historical Fiction", "Fantasy"), 20);
        Analytics analytics2 = new Analytics(0, List.of("Fantasy"), 30);
        Analytics analytics3 = new Analytics(2, List.of("Historical Fiction"), 40);

        assertNotEquals(analytics0, analytics2);
        assertEquals(analytics0, analytics1);
        assertNotEquals(analytics0, analytics3);
    }

    @Test
    void testAnalyticsHashCode() {
        Analytics analytics0 = new Analytics(5, Arrays.asList("Historical Fiction", "Fantasy"), 20);
        Analytics analytics1 = new Analytics(5, Arrays.asList("Historical Fiction", "Fantasy"), 20);

        assertEquals(analytics0.hashCode(), analytics1.hashCode());
    }

    @Test
    void testAnalyticsToString() {
        Analytics analytics = new Analytics(5, Arrays.asList("Historical Fiction", "Fantasy"), 20);

        assertEquals("""
                Analytics:
                \tid = 5
                \tpopularGenres = Historical Fiction, Fantasy
                \tnoLogins = 20""", analytics.toString());
    }

}
