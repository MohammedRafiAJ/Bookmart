package com.bookStore.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.File;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try {
            String configPath = System.getProperty("user.dir") + File.separator + "src" +
                                File.separator + "test" + File.separator + "resources" +
                                File.separator + "config.properties";
            FileInputStream input = new FileInputStream(configPath);
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties file.", e);
        }
    }

    public static String getBaseUri() {
        String value = properties.getProperty("base.uri");
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Missing required config: base.uri");
        }
        return value;
    }

    public static String getContentType() {
        String value = properties.getProperty("content.type");
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Missing required config: content.type");
        }
        return value;
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Missing required config: " + key);
        }
        return value;
    }
}
