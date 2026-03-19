package com.tarecruitment.service;

import com.tarecruitment.dao.UserDAO;
import com.tarecruitment.model.User;

import java.util.List;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public void updateProfile(String userId, String name, String email, 
                             List<String> skills, String availableTime) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
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
}