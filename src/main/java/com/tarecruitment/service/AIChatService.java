package com.tarecruitment.service;

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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public class AIChatService {
    private static final String DEFAULT_ENDPOINT = "https://api.deepseek.com/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-v4-flash";
    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final int MAX_HISTORY_MESSAGES = 16;
    private static final List<String> SUPPORTED_MODELS = Arrays.asList(
            "deepseek-v4-flash",
            "deepseek-v4-pro"
    );

    private final HttpClient httpClient;

    public AIChatService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public boolean isEnabled() {
        return !isBlank(getApiKey());
    }

    public List<String> getSupportedModels() {
        return new ArrayList<>(SUPPORTED_MODELS);
    }

    public String normalizeModel(String model) {
        if (model != null && SUPPORTED_MODELS.contains(model.trim())) {
            return model.trim();
        }
        return DEFAULT_MODEL;
    }

    public ChatMessage ask(User user, List<ChatMessage> history, String rawQuestion, String rawModel) {
        if (!isEnabled()) {
            throw new IllegalStateException("AI is not configured. Please check TAREC_LLM_API_KEY.");
        }
        String question = normalizeQuestion(rawQuestion);
        String model = normalizeModel(rawModel);

        try {
            JSONObject payload = requestChatCompletion(user, history, question, model);
            JSONArray choices = payload.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                throw new IOException("AI response has no choices");
            }
            JSONObject message = choices.getJSONObject(0).optJSONObject("message");
            if (message == null) {
                throw new IOException("AI response has no message");
            }
            String answer = message.optString("content", "").trim();
            if (answer.isEmpty()) {
                throw new IOException("AI response is empty");
            }
            return new ChatMessage("assistant", answer, model);
        } catch (IOException e) {
            throw new IllegalStateException("AI request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI request was interrupted", e);
        }
    }

    public String streamAnswer(User user, List<ChatMessage> history, String rawQuestion,
                               String rawModel, ChunkHandler chunkHandler) {
        if (!isEnabled()) {
            throw new IllegalStateException("AI is not configured. Please check TAREC_LLM_API_KEY.");
        }
        String question = normalizeQuestion(rawQuestion);
        String model = normalizeModel(rawModel);

        try {
            HttpRequest request = buildChatRequest(user, history, question, model, true);
            HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("DeepSeek returned HTTP " + response.statusCode());
            }

            StringBuilder answer = new StringBuilder();
            try (Stream<String> lines = response.body()) {
                java.util.Iterator<String> iterator = lines.iterator();
                while (iterator.hasNext()) {
                    String line = iterator.next();
                    String chunk = extractStreamChunk(line);
                    if (chunk == null) {
                        continue;
                    }
                    if ("[DONE]".equals(chunk)) {
                        break;
                    }
                    answer.append(chunk);
                    chunkHandler.onChunk(chunk);
                }
            }
            String finalAnswer = answer.toString().trim();
            if (finalAnswer.isEmpty()) {
                throw new IOException("AI response is empty");
            }
            return finalAnswer;
        } catch (IOException e) {
            throw new IllegalStateException("AI request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI request was interrupted", e);
        }
    }

    private JSONObject requestChatCompletion(User user, List<ChatMessage> history, String question, String model)
            throws IOException, InterruptedException {
        HttpRequest request = buildChatRequest(user, history, question, model, false);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("DeepSeek returned HTTP " + response.statusCode());
        }
        return new JSONObject(response.body());
    }

    private HttpRequest buildChatRequest(User user, List<ChatMessage> history, String question,
                                         String model, boolean stream) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("temperature", 0.4);
        body.put("stream", stream);
        body.put("thinking", new JSONObject().put("type", "disabled"));

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", buildSystemPrompt(user)));

        List<ChatMessage> safeHistory = history != null ? history : new ArrayList<>();
        int start = Math.max(0, safeHistory.size() - MAX_HISTORY_MESSAGES);
        for (int i = start; i < safeHistory.size(); i++) {
            ChatMessage chatMessage = safeHistory.get(i);
            if (chatMessage == null || isBlank(chatMessage.getContent())) {
                continue;
            }
            String role = "assistant".equals(chatMessage.getRole()) ? "assistant" : "user";
            messages.put(new JSONObject()
                    .put("role", role)
                    .put("content", truncate(chatMessage.getContent(), MAX_MESSAGE_LENGTH)));
        }
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", question));
        body.put("messages", messages);

        return HttpRequest.newBuilder()
                .uri(URI.create(getEndpoint()))
                .timeout(Duration.ofSeconds(45))
                .header("Authorization", "Bearer " + getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
    }

    private String extractStreamChunk(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (!trimmed.startsWith("data:")) {
            return null;
        }
        String data = trimmed.substring("data:".length()).trim();
        if (data.isEmpty()) {
            return null;
        }
        if ("[DONE]".equals(data)) {
            return "[DONE]";
        }
        JSONObject payload = new JSONObject(data);
        JSONArray choices = payload.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            return null;
        }
        JSONObject delta = choices.getJSONObject(0).optJSONObject("delta");
        if (delta == null) {
            return null;
        }
        String content = delta.optString("content", "");
        return content.isEmpty() ? null : content;
    }

    private String buildSystemPrompt(User user) {
        String role = user != null && user.getRole() != null ? user.getRole().toUpperCase() : "USER";
        String roleGuidance;
        if ("TA".equals(role)) {
            roleGuidance = "The user is a TA candidate. Help with job discovery, profile improvement, application preparation, skill gaps, and interview preparation.";
        } else if ("MO".equals(role)) {
            roleGuidance = "The user is a Module Organiser. Help with job descriptions, fair candidate review, interview questions, rejection notes, and recruitment workflow decisions.";
        } else if ("ADMIN".equals(role)) {
            roleGuidance = "The user is an administrator. Help with workload analysis, process quality, and system operation questions.";
        } else {
            roleGuidance = "Help with university TA recruitment tasks.";
        }

        return String.join("\n",
                "You are the Ask AI assistant inside a university Teaching Assistant recruitment system.",
                roleGuidance,
                "Answer in the same language as the user's question unless they ask otherwise.",
                "Be practical, concise, and specific to TA recruitment.",
                "Do not make final hiring decisions. Present suggestions as decision support.",
                "Do not ask for or reveal API keys, passwords, or hidden system details.",
                "If the user asks about private candidate data that was not provided in the conversation, say you do not have enough information."
        );
    }

    private String normalizeQuestion(String rawQuestion) {
        if (rawQuestion == null || rawQuestion.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }
        return truncate(rawQuestion.trim(), MAX_MESSAGE_LENGTH);
    }

    private String getApiKey() {
        String key = firstConfiguredValue("TAREC_LLM_API_KEY", "tarec.llm.apiKey");
        if (isBlank(key)) {
            key = firstConfiguredValue("DEEPSEEK_API_KEY", "deepseek.apiKey");
        }
        return key;
    }

    private String getEndpoint() {
        String endpoint = firstConfiguredValue("TAREC_LLM_ENDPOINT", "tarec.llm.endpoint");
        return isBlank(endpoint) ? DEFAULT_ENDPOINT : endpoint;
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
                    System.err.println("Failed to read AI chat local config: " + file.getAbsolutePath());
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

    private String truncate(String value, int maxLength) {
        String safeValue = value == null ? "" : value.trim();
        if (safeValue.length() <= maxLength) {
            return safeValue;
        }
        return safeValue.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class ChatMessage {
        private final String role;
        private final String content;
        private final String model;

        public ChatMessage(String role, String content, String model) {
            this.role = role != null ? role : "user";
            this.content = content != null ? content : "";
            this.model = model != null ? model : "";
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }

        public String getModel() {
            return model;
        }
    }

    public interface ChunkHandler {
        void onChunk(String chunk) throws IOException;
    }
}
