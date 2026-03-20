package com.tarecruitment.dao;

import com.tarecruitment.model.User;
import com.tarecruitment.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String DATA_FILE = "users.json";
    private JSONObject data;

    public UserDAO() {
        loadData();
    }

    private void loadData() {
        this.data = JsonUtil.readJsonFile(DATA_FILE);
        if (!data.has("users")) {
            data.put("users", new JSONArray());
        }
    }

    private void saveData() {
        JsonUtil.writeJsonFile(DATA_FILE, data);
    }

    public void addUser(User user) {
        JSONArray users = data.getJSONArray("users");
        users.put(userToJson(user));
        saveData();
    }

    public void updateUser(User user) {
        JSONArray users = data.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (u.getString("userId").equals(user.getUserId())) {
                users.put(i, userToJson(user));
                break;
            }
        }
        saveData();
    }

    public void deleteUser(String userId) {
        JSONArray users = data.getJSONArray("users");
        JSONArray newUsers = new JSONArray();
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (!u.getString("userId").equals(userId)) {
                newUsers.put(u);
            }
        }
        data.put("users", newUsers);
        saveData();
    }

    public User getUserById(String userId) {
        JSONArray users = data.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (u.getString("userId").equals(userId)) {
                return jsonToUser(u);
            }
        }
        return null;
    }

    public User getUserByUsername(String username) {
        JSONArray users = data.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (u.getString("username").equals(username)) {
                return jsonToUser(u);
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        JSONArray users = data.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            userList.add(jsonToUser(users.getJSONObject(i)));
        }
        return userList;
    }

    public List<User> getUsersByRole(String role) {
        List<User> userList = new ArrayList<>();
        JSONArray users = data.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (u.getString("role").equalsIgnoreCase(role)) {
                userList.add(jsonToUser(u));
            }
        }
        return userList;
    }

    public List<User> getTAs() {
        return getUsersByRole("TA");
    }

    private JSONObject userToJson(User user) {
        JSONObject u = new JSONObject();
        u.put("userId", user.getUserId());
        u.put("username", user.getUsername());
        u.put("password", user.getPassword());
        u.put("name", user.getName());
        u.put("email", user.getEmail());
        u.put("role", user.getRole());
        u.put("skills", JsonUtil.fromStringList(user.getSkills()));
        u.put("availableTime", user.getAvailableTime() != null ? user.getAvailableTime() : "");
        u.put("bio", user.getBio() != null ? user.getBio() : "");
        u.put("resumeFileName", user.getResumeFileName() != null ? user.getResumeFileName() : "");
        u.put("resumePath", user.getResumePath() != null ? user.getResumePath() : "");
        if (user.getResumeUpdatedAt() != null) {
            u.put("resumeUpdatedAt", user.getResumeUpdatedAt().toString());
        }
        u.put("enabled", user.isEnabled());
        if (user.getCreatedAt() != null) {
            u.put("createdAt", user.getCreatedAt().toString());
        }
        return u;
    }

    private User jsonToUser(JSONObject u) {
        User user = new User();
        user.setUserId(u.getString("userId"));
        user.setUsername(u.getString("username"));
        user.setPassword(u.getString("password"));
        user.setName(u.getString("name"));
        user.setEmail(u.getString("email"));
        user.setRole(u.getString("role"));
        JSONArray skillsArray = u.optJSONArray("skills");
        user.setSkills(JsonUtil.toStringList(skillsArray != null ? skillsArray : new JSONArray()));
        user.setAvailableTime(u.optString("availableTime", ""));
        user.setBio(u.optString("bio", ""));
        String resumeFileName = u.optString("resumeFileName", "");
        user.setResumeFileName(resumeFileName.isEmpty() ? null : resumeFileName);
        String resumePath = u.optString("resumePath", "");
        user.setResumePath(resumePath.isEmpty() ? null : resumePath);
        user.setResumeUpdatedAt(JsonUtil.parseTimestamp(u.optString("resumeUpdatedAt", null)));
        user.setEnabled(u.optBoolean("enabled", true));
        user.setCreatedAt(JsonUtil.parseTimestamp(u.optString("createdAt", null)));
        return user;
    }
}
