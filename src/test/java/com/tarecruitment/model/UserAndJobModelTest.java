package com.tarecruitment.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAndJobModelTest {

    @Test
    void userRoleHelpersIdentifyKnownRoles() {
        User user = new User();

        user.setRole("TA");
        assertTrue(user.isTA());
        assertFalse(user.isMO());

        user.setRole("MO");
        assertTrue(user.isMO());
        assertFalse(user.isAdmin());

        user.setRole("ADMIN");
        assertTrue(user.isAdmin());
    }

    @Test
    void jobDisplayNamesSupportEnglishAndChineseLabels() {
        Job job = new Job();
        job.setType("MODULE");
        job.setStatus("CLOSED");

        assertEquals("Module Tutor", job.getTypeDisplayName());
        assertEquals("课程导师", job.getTypeDisplayName("zh"));
        assertEquals("Closed", job.getStatusDisplayName("en"));
        assertEquals("已关闭", job.getStatusDisplayName("zh"));
    }
}
