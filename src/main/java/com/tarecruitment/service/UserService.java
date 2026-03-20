package com.tarecruitment.service;

import com.tarecruitment.dao.UserDAO;
import com.tarecruitment.model.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public void updateProfile(String userId, String name, String email,
                             List<String> skills, String availableTime) {
        updateProfile(userId, name, email, skills, availableTime, null);
    }

    public void updateProfile(String userId, String name, String email,
                             List<String> skills, String availableTime, String bio) {
        updateProfile(userId, name, email, skills, availableTime, bio,
                null, null, null, null, null);
    }

    public void updateProfile(String userId, String name, String email,
                             List<String> skills, String availableTime, String bio,
                             String availabilityStartDate, String availabilityEndDate, String availabilityWeekdays,
                             String availabilityDailyStartHour, String availabilityDailyEndHour) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        boolean hasAvailabilityInput = !isBlank(availabilityStartDate)
                || !isBlank(availabilityEndDate)
                || !isBlank(availabilityWeekdays)
                || !isBlank(availabilityDailyStartHour)
                || !isBlank(availabilityDailyEndHour);
        String normalizedWeekdays = user.getAvailabilityWeekdays();
        if (hasAvailabilityInput) {
            if (isBlank(availabilityStartDate) || isBlank(availabilityEndDate)
                    || isBlank(availabilityWeekdays) || isBlank(availabilityDailyStartHour)
                    || isBlank(availabilityDailyEndHour)) {
                throw new IllegalArgumentException("Availability schedule fields are required");
            }
            LocalDate startDate = parseDate(availabilityStartDate, "Invalid availability start date");
            LocalDate endDate = parseDate(availabilityEndDate, "Invalid availability end date");
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Availability start date must be on or before end date");
            }
            LocalTime startHour = parseHour(availabilityDailyStartHour, "Invalid availability daily start hour");
            LocalTime endHour = parseHour(availabilityDailyEndHour, "Invalid availability daily end hour");
            if (!startHour.isBefore(endHour)) {
                throw new IllegalArgumentException("Availability daily start hour must be earlier than end hour");
            }
            normalizedWeekdays = normalizeWeekdays(availabilityWeekdays);
            if (normalizedWeekdays.isEmpty()) {
                throw new IllegalArgumentException("Please select at least one weekday");
            }
            availabilityStartDate = startDate.toString();
            availabilityEndDate = endDate.toString();
            availabilityDailyStartHour = startHour.toString();
            availabilityDailyEndHour = endHour.toString();
            availableTime = buildAvailabilitySummary(
                    availabilityStartDate, availabilityEndDate, normalizedWeekdays,
                    availabilityDailyStartHour, availabilityDailyEndHour
            );
        }

        if (name != null) {
            user.setName(name);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (skills != null) {
            user.setSkills(skills);
        }
        if (availableTime != null) {
            user.setAvailableTime(availableTime);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (hasAvailabilityInput) {
            user.setAvailabilityStartDate(availabilityStartDate);
            user.setAvailabilityEndDate(availabilityEndDate);
            user.setAvailabilityWeekdays(normalizedWeekdays);
            user.setAvailabilityDailyStartHour(availabilityDailyStartHour);
            user.setAvailabilityDailyEndHour(availabilityDailyEndHour);
        }

        userDAO.updateUser(user);
    }

    public void updateResume(String userId, String originalFileName, String storedPath) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setResumeFileName(originalFileName);
        user.setResumePath(storedPath);
        user.setResumeUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        userDAO.updateUser(user);
    }

    public User getUserById(String userId) {
        return userDAO.getUserById(userId);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public List<User> getAllTAs() {
        return userDAO.getTAs();
    }

    public List<User> getUsersByRole(String role) {
        return userDAO.getUsersByRole(role);
    }

    public void enableUser(String userId, boolean enabled) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setEnabled(enabled);
        userDAO.updateUser(user);
    }

    private String buildAvailabilitySummary(String startDate, String endDate, String weekdays,
                                            String dailyStartHour, String dailyEndHour) {
        return startDate + " to " + endDate
                + " | " + weekdays
                + " | " + dailyStartHour + "-" + dailyEndHour;
    }

    private String normalizeWeekdays(String weekdaysRaw) {
        if (isBlank(weekdaysRaw)) {
            return "";
        }
        Set<String> valid = new LinkedHashSet<>(Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"));
        Set<String> selected = new LinkedHashSet<>();
        String[] values = weekdaysRaw.split("[,\\s]+");
        for (String value : values) {
            String code = value == null ? "" : value.trim().toUpperCase();
            if (valid.contains(code)) {
                selected.add(code);
            }
        }
        return String.join(",", selected);
    }

    private LocalDate parseDate(String raw, String error) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private LocalTime parseHour(String raw, String error) {
        try {
            LocalTime time = LocalTime.parse(raw.trim());
            if (time.getMinute() != 0 || time.getSecond() != 0 || time.getNano() != 0) {
                throw new IllegalArgumentException(error + " (hour precision required)");
            }
            return time;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
