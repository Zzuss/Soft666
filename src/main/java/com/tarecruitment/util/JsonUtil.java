package com.tarecruitment.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class JsonUtil {

    private static String dataDirectory;
    private static String recoveryWarningMessage;

    public static void setDataDirectory(String dir) {
        dataDirectory = dir;
    }

    private static String getDataDirectory() {
        if (dataDirectory != null) {
            return dataDirectory;
        }
        
        String customPath = System.getProperty("tarec.data.dir");
        if (customPath != null) {
            return customPath;
        }
        
        String webRoot = System.getProperty("user.dir");
        File projectDir = new File(webRoot);
        
        if (new File(projectDir, "data").exists()) {
            return new File(projectDir, "data").getAbsolutePath();
        }
        
        if (new File(projectDir, "src/main/webapp/WEB-INF").exists()) {
            return new File(projectDir, "data").getAbsolutePath();
        }
        
        String dataDir = webRoot + File.separator + "data";
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dataDir;
    }

    public static String getFilePath(String filename) {
        return getDataDirectory() + File.separator + filename;
    }

    public static String getDataDirectoryPath() {
        return getDataDirectory();
    }

    public static JSONObject readJsonFile(String filename) {
        String filePath = getFilePath(filename);
        File file = new File(filePath);

        if (!file.exists()) {
            return new JSONObject();
        }

        try {
            return readJsonFromFile(file);
        } catch (Exception e) {
            JSONObject recovered = tryRecoverFromBackup(filename, e);
            if (recovered != null) {
                return recovered;
            }
            setRecoveryWarningMessage(
                    "Failed to read data file '" + filename + "' and backup. System started with empty data."
            );
            System.err.println("Error reading JSON file and backup for " + filename + ": " + e.getMessage());
            return new JSONObject();
        }
    }

    public static void writeJsonFile(String filename, JSONObject jsonObject) {
        String filePath = getFilePath(filename);
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.err.println("Error creating data directory: " + parent.getAbsolutePath());
            return;
        }

        try {
            backupCurrentFile(file.toPath());
            writeWithTempFile(file.toPath(), jsonObject);
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }
    }

    public static synchronized String consumeRecoveryWarning() {
        String warning = recoveryWarningMessage;
        recoveryWarningMessage = null;
        return warning;
    }

    public static JSONArray getJsonArray(JSONObject json, String key) {
        if (json.has(key) && !json.isNull(key)) {
            return json.getJSONArray(key);
        }
        return new JSONArray();
    }

    public static List<String> toStringList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        }
        return list;
    }

    public static JSONArray fromStringList(List<String> list) {
        JSONArray array = new JSONArray();
        if (list != null) {
            for (String s : list) {
                array.put(s);
            }
        }
        return array;
    }

    public static Timestamp parseTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof String) {
            try {
                return Timestamp.valueOf((String) value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public static String generateId(String prefix) {
        long now = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return prefix + now + random;
    }

    private static JSONObject readJsonFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JSONTokener tokener = new JSONTokener(reader);
            return new JSONObject(tokener);
        }
    }

    private static JSONObject tryRecoverFromBackup(String filename, Exception originalException) {
        File backupFile = new File(getFilePath(filename) + ".bak");
        if (!backupFile.exists()) {
            return null;
        }
        try {
            JSONObject recovered = readJsonFromFile(backupFile);
            setRecoveryWarningMessage(
                    "Recovered '" + filename + "' from backup due to data corruption."
            );
            System.err.println("Recovered from backup for " + filename + ". Original error: "
                    + originalException.getMessage());
            return recovered;
        } catch (Exception backupException) {
            System.err.println("Backup recovery failed for " + filename + ": " + backupException.getMessage());
            return null;
        }
    }

    private static void backupCurrentFile(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            return;
        }
        Path backupPath = Path.of(filePath.toString() + ".bak");
        Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void writeWithTempFile(Path filePath, JSONObject jsonObject) throws IOException {
        Path tempPath = Path.of(filePath.toString() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tempPath)) {
            jsonObject.write(writer, 2, 0);
        }
        try {
            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempPath, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static synchronized void setRecoveryWarningMessage(String message) {
        recoveryWarningMessage = message;
    }
}
