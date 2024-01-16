package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.analytics.Analytics;
import nl.tudelft.sem.template.example.domain.analytics.AnalyticsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AnalyticsControllerTest {

    private static AnalyticsService analyticsService;
    private static AnalyticsController controller;

    @BeforeAll
    static void setup() {
        analyticsService = Mockito.mock(AnalyticsService.class);
        controller = new AnalyticsController(analyticsService);
    }

    @Test
    void testGetAnalytics() {
        Analytics analytics = new Analytics(1, List.of("Fiction", "Fantasy"), 25);
        when(analyticsService.compileAnalytics()).thenReturn(analytics);

        ResponseEntity<Analytics> response = controller.analyticsGet();

        assertEquals(analytics, response.getBody());
    }

}
