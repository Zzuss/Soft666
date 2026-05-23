package com.tarecruitment.service;

import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingServiceTest {

    private final MatchingService matchingService = new MatchingService();

    @Test
    void evaluateGivesFullScoreWhenApplicantHasAllRequiredSkills() {
        Job job = new Job();
        job.setRequiredSkills("Java, SQL, communication");

        User applicant = new User();
        applicant.setSkills(List.of("Java", "MySQL", "presentation"));

        MatchingService.MatchResult result = matchingService.evaluate(job, applicant);

        assertEquals(100.0, result.getScore(), 0.001);
        assertTrue(result.getMatchedSkills().containsAll(List.of("java", "sql", "communication")));
        assertTrue(result.getMissingSkills().isEmpty());
    }

    @Test
    void evaluateReportsMissingSkills() {
        Job job = new Job();
        job.setRequiredSkills("Python, machine learning");

        User applicant = new User();
        applicant.setSkills(List.of("Python"));

        MatchingService.MatchResult result = matchingService.evaluate(job, applicant);

        assertEquals(50.0, result.getScore(), 0.001);
        assertEquals(List.of("python"), result.getMatchedSkills());
        assertEquals(List.of("machine learning"), result.getMissingSkills());
    }
}
