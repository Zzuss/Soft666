package com.tarecruitment.dao;

import com.tarecruitment.model.Application;
import com.tarecruitment.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {
    private static final String DATA_FILE = "applications.json";
    private JSONObject data;

    public ApplicationDAO() {
        loadData();
    }

    private void loadData() {
        this.data = JsonUtil.readJsonFile(DATA_FILE);
        if (!data.has("applications")) {
            data.put("applications", new JSONArray());
        }
    }

    private void saveData() {
        JsonUtil.writeJsonFile(DATA_FILE, data);
    }

    public void addApplication(Application app) {
        JSONArray apps = data.getJSONArray("applications");
        apps.put(applicationToJson(app));
        saveData();
    }

    public void updateApplication(Application app) {
        JSONArray apps = data.getJSONArray("applications");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject a = apps.getJSONObject(i);
            if (a.getString("applicationId").equals(app.getApplicationId())) {
                apps.put(i, applicationToJson(app));
                break;
            }
        }
        saveData();
    }

    public void deleteApplication(String applicationId) {
        JSONArray apps = data.getJSONArray("applications");
        JSONArray newApps = new JSONArray();
        for (int i = 0; i < apps.length(); i++) {
            JSONObject a = apps.getJSONObject(i);
            if (!a.getString("applicationId").equals(applicationId)) {
                newApps.put(a);
            }
        }
        data.put("applications", newApps);
        saveData();
    }

    public Application getApplicationById(String applicationId) {
        JSONArray apps = data.getJSONArray("applications");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject a = apps.getJSONObject(i);
            if (a.getString("applicationId").equals(applicationId)) {
                return jsonToApplication(a);
            }
        }
        return null;
    }

    public Application getApplication(String jobId, String userId) {
        JSONArray apps = data.getJSONArray("applications");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject a = apps.getJSONObject(i);
            if (a.getString("jobId").equals(jobId) && a.getString("userId").equals(userId)) {
                return jsonToApplication(a);
            }
        }
        return null;
    }

    public List<Application> getApplicationsByJob(String jobId) {
        List<Application> appList = new ArrayList<>();
        JSONArray apps = data.getJSONArray("applications");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject a = apps.getJSONObject(i);
            if (a.getString("jobId").equals(jobId)) {
                appList.add(jsonToApplication(a));
            }
        }
        return appList;
    }

    public List<Application> getApplicationsByUser(String userId) {
        List<Application> appList = new ArrayList<>();
        JSONArray apps = data.getJSONArray("applications");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject a = apps.getJSONObject(i);
            if (a.getString("userId").equals(userId)) {
                appList.add(jsonToApplication(a));
            }
        }
        return appList;
    }

    public List<Application> getApprovedApplicationsByUser(String userId) {
        List<Application> appList = new ArrayList<>();
        JSONArray apps = data.getJSONArray("applications");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject a = apps.getJSONObject(i);
            if (a.getString("userId").equals(userId) && "APPROVED".equalsIgnoreCase(a.optString("status"))) {
                appList.add(jsonToApplication(a));
            }
        }
        return appList;
    }

    public boolean hasApplied(String jobId, String userId) {
        return getApplication(jobId, userId) != null;
    }

    private JSONObject applicationToJson(Application app) {
        JSONObject a = new JSONObject();
        a.put("applicationId", app.getApplicationId());
        a.put("jobId", app.getJobId());
        a.put("userId", app.getUserId());
        a.put("status", app.getStatus());
        if (app.getAppliedAt() != null) {
            a.put("appliedAt", app.getAppliedAt().toString());
        }
        a.put("reviewedBy", app.getReviewedBy() != null ? app.getReviewedBy() : "");
        if (app.getReviewedAt() != null) {
            a.put("reviewedAt", app.getReviewedAt().toString());
        }
        return a;
    }

    private Application jsonToApplication(JSONObject a) {
        Application app = new Application();
        app.setApplicationId(a.getString("applicationId"));
        app.setJobId(a.getString("jobId"));
        app.setUserId(a.getString("userId"));
        app.setStatus(a.optString("status", "PENDING"));
        app.setAppliedAt(JsonUtil.parseTimestamp(a.optString("appliedAt", null)));
        String reviewedBy = a.optString("reviewedBy", "");
        app.setReviewedBy(reviewedBy.isEmpty() ? null : reviewedBy);
        app.setReviewedAt(JsonUtil.parseTimestamp(a.optString("reviewedAt", null)));
        return app;
    }
}