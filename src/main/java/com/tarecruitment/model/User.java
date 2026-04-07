package com.tarecruitment.model;

import java.sql.Timestamp;
import java.util.List;

public class User {
    private String userId;
    private String username;
    private String password;
    private String name;
    private String email;
    private String role;
    private String studentId;
    private String major;
    private int year;
    private String phone;
    private List<String> skills;
    private String availableTime;
    private String availabilityStartDate;
    private String availabilityEndDate;
    private String availabilityWeekdays;
    private String availabilityDailyStartHour;
    private String availabilityDailyEndHour;
    private String bio;
    private String resumeFileName;
    private String resumePath;
    private Timestamp resumeUpdatedAt;
    private boolean enabled;
    private Timestamp createdAt;

    public User() {
    }

    public User(String userId, String username, String password, String name, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
        this.enabled = true;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public String getAvailabilityStartDate() {
        return availabilityStartDate;
    }

    public void setAvailabilityStartDate(String availabilityStartDate) {
        this.availabilityStartDate = availabilityStartDate;
    }

    public String getAvailabilityEndDate() {
        return availabilityEndDate;
    }

    public void setAvailabilityEndDate(String availabilityEndDate) {
        this.availabilityEndDate = availabilityEndDate;
    }

    public String getAvailabilityWeekdays() {
        return availabilityWeekdays;
    }

    public void setAvailabilityWeekdays(String availabilityWeekdays) {
        this.availabilityWeekdays = availabilityWeekdays;
    }

    public String getAvailabilityDailyStartHour() {
        return availabilityDailyStartHour;
    }

    public void setAvailabilityDailyStartHour(String availabilityDailyStartHour) {
        this.availabilityDailyStartHour = availabilityDailyStartHour;
    }

    public String getAvailabilityDailyEndHour() {
        return availabilityDailyEndHour;
    }

    public void setAvailabilityDailyEndHour(String availabilityDailyEndHour) {
        this.availabilityDailyEndHour = availabilityDailyEndHour;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public Timestamp getResumeUpdatedAt() {
        return resumeUpdatedAt;
    }

    public void setResumeUpdatedAt(Timestamp resumeUpdatedAt) {
        this.resumeUpdatedAt = resumeUpdatedAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isTA() {
        return "TA".equalsIgnoreCase(role);
    }

    public boolean isMO() {
        return "MO".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
