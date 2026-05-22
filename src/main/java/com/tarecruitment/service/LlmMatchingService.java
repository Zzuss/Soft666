package com.tarecruitment.service;

import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class LlmMatchingService {
    private static final String DEFAULT_ENDPOINT = "https://api.deepseek.com/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-v4-flash";
    private static final int MAX_TEXT_LENGTH = 1200;
    private static final String SYSTEM_PROMPT = String.join("\n",
            "You are a fair and conservative matching assistant for a university Teaching Assistant recruitment system.",
            "Your task is to estimate candidate-job fit using only the structured job and candidate data supplied by the application.",
            "Do not invent experience, credentials, availability, or skills that are not supported by the supplied data.",
            "Do not use protected or irrelevant personal attributes. Ignore names, email style, student ID, gender, age, nationality, race, ethnicity, religion, disability, and any inferred background.",
            "Major, skills, experience text, course relevance, teaching relevance, and schedule compatibility may be used when they are directly relevant to the job.",
            "The baseline rule score is a weak reference from keyword matching, not a final decision. You may adjust it when the text evidence supports a higher or lower fit.",
            "Return valid compact JSON only. Do not include markdown, comments, or extra keys.",
            "Required JSON schema: {\"score\": number, \"matchedSkills\": string[], \"missingSkills\": string[], \"explanation\": string}."
    );

    private final HttpClient httpClient;

    public LlmMatchingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public boolean isEnabled() {
        return !isBlank(getApiKey());
    }

    public MatchingService.MatchResult evaluate(Job job, User applicant, MatchingService.MatchResult fallback) {
        if (!isEnabled()) {
            return fallback;
        }
        try {
            JSONObject response = requestMatchAnalysis(job, applicant, fallback);
            return parseMatchResult(response, fallback);
        } catch (Exception e) {
            return fallback;
        }
    }

    private JSONObject requestMatchAnalysis(Job job, User applicant, MatchingService.MatchResult fallback)
            throws IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("model", getModel());
        body.put("temperature", 0.2);
        body.put("response_format", new JSONObject().put("type", "json_object"));
        body.put("thinking", new JSONObject().put("type", "disabled"));

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", SYSTEM_PROMPT));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", buildPrompt(job, applicant, fallback)));
        body.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getEndpoint()))
                .timeout(Duration.ofSeconds(18))
                .header("Authorization", "Bearer " + getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("LLM request failed: " + response.statusCode());
        }

        JSONObject payload = new JSONObject(response.body());
        JSONArray choices = payload.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new IOException("LLM response has no choices");
        }
        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        if (message == null) {
            throw new IOException("LLM response has no message");
        }
        String content = message.optString("content", "");
        if (isBlank(content)) {
            throw new IOException("LLM response content is empty");
        }
        return new JSONObject(stripJsonFence(content));
    }

    private String buildPrompt(Job job, User applicant, MatchingService.MatchResult fallback) {
        JSONObject input = new JSONObject();
        input.put("baselineRuleScore", fallback.getScore());
        input.put("baselineMatchedSkills", new JSONArray(fallback.getMatchedSkills()));
        input.put("baselineMissingSkills", new JSONArray(fallback.getMissingSkills()));

        JSONObject jobJson = new JSONObject();
        jobJson.put("title", safe(job.getTitle()));
        jobJson.put("courseCode", safe(job.getCourseCode()));
        jobJson.put("type", safe(job.getType()));
        jobJson.put("description", truncate(job.getDescription()));
        jobJson.put("requirements", truncate(job.getRequirements()));
        jobJson.put("requiredSkills", truncate(job.getRequiredSkills()));
        jobJson.put("workSchedule", safe(job.getWorkStartDate()) + " to " + safe(job.getWorkEndDate())
                + " | " + safe(job.getWorkWeekdays())
                + " | " + safe(job.getDailyStartHour()) + "-" + safe(job.getDailyEndHour()));
        input.put("job", jobJson);

        JSONObject applicantJson = new JSONObject();
        applicantJson.put("major", safe(applicant.getMajor()));
        applicantJson.put("skills", applicant.getSkills() != null ? new JSONArray(applicant.getSkills()) : new JSONArray());
        applicantJson.put("bio", truncate(applicant.getBio()));
        applicantJson.put("availability", safe(applicant.getAvailableTime()));
        applicantJson.put("resumeFileName", safe(applicant.getResumeFileName()));
        input.put("candidate", applicantJson);

        return String.join("\n",
                "Evaluate this TA application using the rubric below.",
                "",
                "Rubric:",
                "- Technical/subject skill match: 40%. Consider explicit requiredSkills first, then requirements, description, and course type.",
                "- Teaching or TA relevance: 20%. Give credit for tutoring, grading, lab support, communication, presentation, or exam supervision evidence.",
                "- Course/domain relevance: 15%. Consider whether the candidate major, skills, and bio fit the course or job context.",
                "- Schedule compatibility: 15%. Compare job workSchedule with candidate availability. Penalize clear conflicts or missing availability.",
                "- Profile evidence quality: 10%. Prefer candidates with concrete, relevant evidence over vague or empty profiles.",
                "",
                "Score calibration:",
                "- 90-100: excellent fit with strong direct evidence and no important gaps.",
                "- 75-89: strong fit with minor gaps.",
                "- 60-74: usable fit but some important uncertainty or gaps.",
                "- 40-59: weak fit; several requirements are missing or unclear.",
                "- 0-39: poor fit or clear schedule/skill mismatch.",
                "",
                "Output rules:",
                "- score must be a number from 0 to 100 with at most one decimal place.",
                "- matchedSkills should list short evidence-backed skills or strengths.",
                "- missingSkills should list only important missing or uncertain requirements; use [] if no important gap is found.",
                "- explanation must be one concise Chinese sentence, suitable to show to both TA and MO.",
                "- If evidence is missing, say it is missing or unclear; do not guess.",
                "",
                "Application data:",
                input.toString()
        );
    }

    private MatchingService.MatchResult parseMatchResult(JSONObject response, MatchingService.MatchResult fallback) {
        double score = clampScore(response.optDouble("score", fallback.getScore()));
        JSONArray matchedSkillsJson = response.optJSONArray("matchedSkills");
        JSONArray missingSkillsJson = response.optJSONArray("missingSkills");
        List<String> matchedSkills = readStringList(matchedSkillsJson);
        List<String> missingSkills = readStringList(missingSkillsJson);
        String explanation = truncate(response.optString("explanation", ""));

        if (matchedSkillsJson == null) {
            matchedSkills = fallback.getMatchedSkills();
        }
        if (missingSkillsJson == null) {
            missingSkills = fallback.getMissingSkills();
        }
        if (isBlank(explanation)) {
            explanation = "AI 已根据候选人档案与岗位要求生成综合匹配评估。";
        }
        return new MatchingService.MatchResult(score, matchedSkills, missingSkills, "LLM", explanation);
    }

    private List<String> readStringList(JSONArray array) {
        List<String> values = new ArrayList<>();
        if (array == null) {
            return values;
        }
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "").trim();
            if (!value.isEmpty() && !values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private double clampScore(double rawScore) {
        double score = Math.max(0.0, Math.min(100.0, rawScore));
        return Math.round(score * 10.0) / 10.0;
    }

    private String getApiKey() {
        String key = firstConfiguredValue("TAREC_LLM_API_KEY", "tarec.llm.apiKey");
        if (isBlank(key)) {
            key = firstConfiguredValue("DEEPSEEK_API_KEY", "deepseek.apiKey");
        }
        if (isBlank(key)) {
            key = firstConfiguredValue("OPENAI_API_KEY", "openai.apiKey");
        }
        return key;
    }

    private String getEndpoint() {
        String endpoint = firstConfiguredValue("TAREC_LLM_ENDPOINT", "tarec.llm.endpoint");
        return isBlank(endpoint) ? DEFAULT_ENDPOINT : endpoint;
    }

    private String getModel() {
        String model = firstConfiguredValue("TAREC_LLM_MODEL", "tarec.llm.model");
        return isBlank(model) ? DEFAULT_MODEL : model;
    }

    private String firstConfiguredValue(String envKey, String propertyKey) {
        String value = System.getenv(envKey);
        if (!isBlank(value)) {
            return value.trim();
        }
        value = System.getProperty(propertyKey);
        if (!isBlank(value)) {
            return value.trim();
        }
        Properties localProperties = loadLocalProperties();
        value = localProperties.getProperty(envKey);
        if (!isBlank(value)) {
            return value.trim();
        }
        return localProperties.getProperty(propertyKey);
    }

    private Properties loadLocalProperties() {
        Properties properties = new Properties();
        for (File file : getLocalConfigCandidates()) {
            if (file.exists() && file.isFile()) {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    properties.load(inputStream);
                } catch (IOException e) {
                    System.err.println("Failed to read LLM local config: " + file.getAbsolutePath());
                }
                break;
            }
        }
        return properties;
    }

    private Set<File> getLocalConfigCandidates() {
        Set<File> files = new LinkedHashSet<>();
        String explicitConfigPath = System.getProperty("tarec.llm.config");
        if (!isBlank(explicitConfigPath)) {
            files.add(new File(explicitConfigPath));
        }
        files.add(new File(".env.local"));
        File dataDir = new File(JsonUtil.getDataDirectoryPath());
        File projectDir = dataDir.getParentFile();
        if (projectDir != null) {
            files.add(new File(projectDir, ".env.local"));
        }
        return files;
    }

    private String truncate(String value) {
        String safeValue = safe(value);
        if (safeValue.length() <= MAX_TEXT_LENGTH) {
            return safeValue;
        }
        return safeValue.substring(0, MAX_TEXT_LENGTH);
    }

    private String stripJsonFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstNewline = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstNewline < 0 || lastFence <= firstNewline) {
            return trimmed;
        }
        return trimmed.substring(firstNewline + 1, lastFence).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
