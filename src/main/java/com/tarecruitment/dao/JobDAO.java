package com.tarecruitment.dao;

import com.tarecruitment.model.Job;
import com.tarecruitment.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JobDAO {
    private static final String DATA_FILE = "jobs.json";
    private JSONObject data;

    public JobDAO() {
        loadData();
    }

    private void loadData() {
        this.data = JsonUtil.readJsonFile(DATA_FILE);
        if (!data.has("jobs")) {
            data.put("jobs", new JSONArray());
        }
    }

    private void saveData() {
        JsonUtil.writeJsonFile(DATA_FILE, data);
    }

    public void addJob(Job job) {
        loadData();
        JSONArray jobs = data.getJSONArray("jobs");
        jobs.put(jobToJson(job));
        saveData();
    }

    public void updateJob(Job job) {
        loadData();
        JSONArray jobs = data.getJSONArray("jobs");
        for (int i = 0; i < jobs.length(); i++) {
            JSONObject j = jobs.getJSONObject(i);
            if (j.getString("jobId").equals(job.getJobId())) {
                jobs.put(i, jobToJson(job));
                break;
            }
        }
        saveData();
    }

    public void deleteJob(String jobId) {
        loadData();
        JSONArray jobs = data.getJSONArray("jobs");
        JSONArray newJobs = new JSONArray();
        for (int i = 0; i < jobs.length(); i++) {
            JSONObject j = jobs.getJSONObject(i);
            if (!j.getString("jobId").equals(jobId)) {
                newJobs.put(j);
            }
        }
        data.put("jobs", newJobs);
        saveData();
    }

    public Job getJobById(String jobId) {
        loadData();
        JSONArray jobs = data.getJSONArray("jobs");
        for (int i = 0; i < jobs.length(); i++) {
            JSONObject j = jobs.getJSONObject(i);
            if (j.getString("jobId").equals(jobId)) {
                return jsonToJob(j);
            }
        }
        return null;
    }

    public List<Job> getAllJobs() {
        loadData();
        List<Job> jobList = new ArrayList<>();
        JSONArray jobs = data.getJSONArray("jobs");
        for (int i = 0; i < jobs.length(); i++) {
            jobList.add(jsonToJob(jobs.getJSONObject(i)));
        }
        return jobList;
    }

    public List<Job> getOpenJobs() {
        loadData();
        List<Job> jobList = new ArrayList<>();
        JSONArray jobs = data.getJSONArray("jobs");
        for (int i = 0; i < jobs.length(); i++) {
            JSONObject j = jobs.getJSONObject(i);
            if ("OPEN".equalsIgnoreCase(j.optString("status", "OPEN"))) {
                jobList.add(jsonToJob(j));
            }
        }
        return jobList;
    }

    public List<Job> getJobsByMo(String moId) {
        loadData();
        List<Job> jobList = new ArrayList<>();
        JSONArray jobs = data.getJSONArray("jobs");
        for (int i = 0; i < jobs.length(); i++) {
            JSONObject j = jobs.getJSONObject(i);
            if (j.getString("postedBy").equals(moId)) {
                jobList.add(jsonToJob(j));
            }
        }
        return jobList;
    }

    public List<Job> getJobsByType(String type) {
        loadData();
        List<Job> jobList = new ArrayList<>();
        JSONArray jobs = data.getJSONArray("jobs");
        for (int i = 0; i < jobs.length(); i++) {
            JSONObject j = jobs.getJSONObject(i);
            if (j.getString("type").equalsIgnoreCase(type) && "OPEN".equalsIgnoreCase(j.optString("status", "OPEN"))) {
                jobList.add(jsonToJob(j));
            }
        }
        return jobList;
    }

    private JSONObject jobToJson(Job job) {
        JSONObject j = new JSONObject();
        j.put("jobId", job.getJobId());
        j.put("title", job.getTitle());
        j.put("type", job.getType());
        j.put("description", job.getDescription());
        j.put("requirements", job.getRequirements());
        j.put("workStartDate", job.getWorkStartDate() != null ? job.getWorkStartDate() : "");
        j.put("workEndDate", job.getWorkEndDate() != null ? job.getWorkEndDate() : "");
        j.put("workWeekdays", job.getWorkWeekdays() != null ? job.getWorkWeekdays() : "");
        j.put("dailyStartHour", job.getDailyStartHour() != null ? job.getDailyStartHour() : "");
        j.put("dailyEndHour", job.getDailyEndHour() != null ? job.getDailyEndHour() : "");
        j.put("positions", job.getPositions());
        j.put("postedBy", job.getPostedBy());
        j.put("deadline", job.getDeadline());
        j.put("status", job.getStatus());
        if (job.getCreatedAt() != null) {
            j.put("createdAt", job.getCreatedAt().toString());
        }
        return j;
    }

    private Job jsonToJob(JSONObject j) {
        Job job = new Job();
        job.setJobId(j.getString("jobId"));
        job.setTitle(j.getString("title"));
        job.setType(j.getString("type"));
        job.setDescription(j.getString("description"));
        job.setRequirements(j.getString("requirements"));
        job.setWorkStartDate(j.optString("workStartDate", ""));
        job.setWorkEndDate(j.optString("workEndDate", ""));
        job.setWorkWeekdays(j.optString("workWeekdays", ""));
        job.setDailyStartHour(j.optString("dailyStartHour", ""));
        job.setDailyEndHour(j.optString("dailyEndHour", ""));
        job.setPositions(j.getInt("positions"));
        job.setPostedBy(j.getString("postedBy"));
        job.setDeadline(j.getString("deadline"));
        job.setStatus(j.optString("status", "OPEN"));
        job.setCreatedAt(JsonUtil.parseTimestamp(j.optString("createdAt", null)));
        return job;
    }
}
