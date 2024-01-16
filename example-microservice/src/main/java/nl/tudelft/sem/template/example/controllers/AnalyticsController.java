package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.domain.analytics.Analytics;
import nl.tudelft.sem.template.example.domain.analytics.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {

    AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/analytics"
    )
    public ResponseEntity<Analytics> analyticsGet() {
        try {
            Analytics analytics = analyticsService.compileAnalytics();
            return ResponseEntity.ok(analytics);
        }
        catch(Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
