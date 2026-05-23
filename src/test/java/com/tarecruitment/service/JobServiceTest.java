package com.tarecruitment.service;

import com.tarecruitment.model.Job;
import com.tarecruitment.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobServiceTest {

    @TempDir
    Path tempDataDir;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        JsonUtil.setDataDirectory(tempDataDir.toString());
        jobService = new JobService();
    }

    @Test
    void createJobNormalizesScheduleFieldsAndPersistsOpenJob() {
        Job job = jobService.createJob(
                "Java TA",
                "module",
                "Support tutorials",
                "Good Java knowledge",
                2,
                "2026-06-30",
                "mo-1",
                "COMP101",
                "Java, Communication",
                "2026-07-01",
                "2026-07-31",
                "mon wed FRI mon",
                "09:00",
                "11:00"
        );

        assertEquals("Java TA", job.getTitle());
        assertEquals("MODULE", job.getType());
        assertEquals("MON,WED,FRI", job.getWorkWeekdays());
        assertEquals("OPEN", job.getStatus());
        assertEquals(1, jobService.getOpenJobs().size());
    }

    @Test
    void createJobRejectsInvalidSchedule() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jobService.createJob(
                        "Invalid TA",
                        "module",
                        "Support tutorials",
                        "Good Java knowledge",
                        1,
                        "2026-06-30",
                        "mo-1",
                        "COMP101",
                        "Java",
                        "2026-07-31",
                        "2026-07-01",
                        "MON",
                        "09:00",
                        "11:00"
                )
        );

        assertTrue(exception.getMessage().contains("Work start date"));
    }

    @Test
    void supportedSkillsIncludesCoreSkills() {
        List<String> skills = jobService.getSupportedSkills();

        assertTrue(skills.contains("Java"));
        assertTrue(skills.contains("Python"));
        assertTrue(skills.contains("Communication"));
    }
}
