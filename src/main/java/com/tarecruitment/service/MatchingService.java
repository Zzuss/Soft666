package com.tarecruitment.service;

import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchingService {

    private static final Map<String, List<String>> SKILL_DICT = new LinkedHashMap<>();
    private static final Map<String, String> ALIAS_TO_CANONICAL = new LinkedHashMap<>();
    private static final String EXPLICIT_SKILL_SPLIT_REGEX = "[,，;；、/／\\\\|\\n\\r]+";

    static {
        addSkill("java", "java", "spring", "spring boot");
        addSkill("python", "python", "pandas", "numpy");
        addSkill("sql", "sql", "mysql", "postgresql", "database");
        addSkill("javascript", "javascript", "js", "node.js", "nodejs", "react", "vue");
        addSkill("html/css", "html", "css", "frontend");
        addSkill("data analysis", "data analysis", "analytics", "excel", "power bi");
        addSkill("machine learning", "machine learning", "ml");
        addSkill("teaching", "teaching", "tutor", "tutoring", "teaching assistant", "ta");
        addSkill("communication", "communication", "presentation");
        addSkill("teamwork", "teamwork", "collaboration");
        addSkill("invigilation", "invigilation", "exam supervision", "exam");
    }

    public MatchResult evaluate(Job job, User applicant) {
        Set<String> requiredSkills = extractJobSkills(job);
        Set<String> applicantSkills = extractApplicantSkills(applicant);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String skill : requiredSkills) {
            if (applicantSkills.contains(skill)) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }

        double score;
        if (requiredSkills.isEmpty()) {
            score = applicantSkills.isEmpty() ? 0.0 : 50.0;
        } else {
            score = (matched.size() * 100.0) / requiredSkills.size();
        }
        score = Math.round(score * 10.0) / 10.0;
        return new MatchResult(score, matched, missing);
    }

    private Set<String> extractJobSkills(Job job) {
        Set<String> skills = new LinkedHashSet<>();
        if (job == null) {
            return skills;
        }
        String preferredRequiredSkills = job.getRequiredSkills();
        String fallbackRequirements = job.getRequirements();
        String sourceText = preferredRequiredSkills != null && !preferredRequiredSkills.trim().isEmpty()
                ? preferredRequiredSkills
                : fallbackRequirements;

        skills.addAll(extractExplicitSkills(sourceText));
        skills.addAll(extractSkillsFromText(sourceText));
        if (skills.isEmpty()) {
            skills.addAll(extractSkillsFromText(job.getDescription()));
            skills.addAll(extractSkillsFromText(job.getTitle()));
            skills.addAll(extractSkillsFromText(job.getType()));
        }
        return skills;
    }

    private Set<String> extractApplicantSkills(User applicant) {
        Set<String> skills = new LinkedHashSet<>();
        if (applicant == null) {
            return skills;
        }
        if (applicant.getSkills() != null) {
            for (String skill : applicant.getSkills()) {
                skills.addAll(extractExplicitSkills(skill));
                skills.addAll(extractSkillsFromText(skill));
            }
        }
        skills.addAll(extractSkillsFromText(applicant.getBio()));
        return skills;
    }

    private Set<String> extractSkillsFromText(String text) {
        Set<String> result = new LinkedHashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        List<String> textTokens = tokenize(text);
        for (Map.Entry<String, List<String>> entry : SKILL_DICT.entrySet()) {
            for (String alias : entry.getValue()) {
                if (containsAlias(textTokens, alias)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }

    private Set<String> extractExplicitSkills(String text) {
        Set<String> result = new LinkedHashSet<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        String[] chunks = text.split(EXPLICIT_SKILL_SPLIT_REGEX);
        if (chunks.length == 1) {
            addNormalizedSkill(result, normalizeForLookup(chunks[0]));
            return result;
        }
        for (String chunk : chunks) {
            String[] parts = chunk.split("(?i)\\b(?:and|or)\\b");
            for (String part : parts) {
                addNormalizedSkill(result, normalizeForLookup(part));
            }
        }
        return result;
    }

    private void addNormalizedSkill(Set<String> result, String normalized) {
        if (!isLikelySkillPhrase(normalized)) {
            return;
        }
        String canonical = ALIAS_TO_CANONICAL.get(normalized);
        result.add(canonical != null ? canonical : normalized);
    }

    private boolean isLikelySkillPhrase(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return false;
        }
        return normalized.split("\\s+").length <= 4;
    }

    private boolean containsAlias(List<String> textTokens, String alias) {
        List<String> aliasTokens = tokenize(alias);
        if (aliasTokens.isEmpty() || textTokens.isEmpty() || aliasTokens.size() > textTokens.size()) {
            return false;
        }
        for (int i = 0; i <= textTokens.size() - aliasTokens.size(); i++) {
            boolean allMatch = true;
            for (int j = 0; j < aliasTokens.size(); j++) {
                if (!textTokens.get(i + j).equals(aliasTokens.get(j))) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                return true;
            }
        }
        return false;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null) {
            return tokens;
        }
        String[] parts = text.toLowerCase().split("[^a-z0-9]+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private static String normalizeForLookup(String raw) {
        if (raw == null) {
            return "";
        }
        String[] parts = raw.toLowerCase().split("[^a-z0-9]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(part);
        }
        return sb.toString();
    }

    private static void addSkill(String canonical, String... aliases) {
        List<String> values = Arrays.asList(aliases);
        SKILL_DICT.put(canonical, values);
        for (String alias : values) {
            ALIAS_TO_CANONICAL.put(normalizeForLookup(alias), canonical);
        }
    }

    public static class MatchResult {
        private final double score;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;

        public MatchResult(double score, List<String> matchedSkills, List<String> missingSkills) {
            this.score = score;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
        }

        public double getScore() {
            return score;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }
    }
}
