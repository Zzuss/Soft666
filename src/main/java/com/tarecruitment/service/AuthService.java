package com.tarecruitment.service;

import com.tarecruitment.dao.UserDAO;
import com.tarecruitment.model.User;
import com.tarecruitment.util.JsonUtil;
import com.tarecruitment.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;
import java.util.List;

public class AuthService {
    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User register(String username, String password, String name, String email, String role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userDAO.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUserId(JsonUtil.generateId("u"));
        user.setUsername(username.trim());
        user.setPassword(PasswordUtil.encrypt(password));
        user.setName(name);
        user.setEmail(email);
        user.setRole(role != null ? role.toUpperCase() : "TA");
        user.setEnabled(true);

        userDAO.addUser(user);
        return user;
    }

    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return null;
        }

        if (!user.isEnabled()) {
            return null;
        }

        if (PasswordUtil.verify(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    public boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user") != null;
    }

    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    public boolean hasRole(HttpSession session, String role) {
        User user = getCurrentUser(session);
        return user != null && user.getRole().equalsIgnoreCase(role);
    }
}