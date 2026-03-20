package com.tarecruitment.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    private static String dataDirectory;

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

        try (FileReader reader = new FileReader(file)) {
            JSONTokener tokener = new JSONTokener(reader);
            return new JSONObject(tokener);
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            return new JSONObject();
        }
    }

    public static void writeJsonFile(String filename, JSONObject jsonObject) {
        String filePath = getFilePath(filename);
        File file = new File(filePath);
        
        try (FileWriter writer = new FileWriter(file)) {
            jsonObject.write(writer, 2, 0);
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }
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
        return prefix + System.currentTimeMillis();
    }
}
