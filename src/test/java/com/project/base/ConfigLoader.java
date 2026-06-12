package com.project.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;

        loadFile("config/config.properties", true);

        String env = System.getProperty("env");
        if (env != null && !env.isBlank()) {
            loadFile("config/env/" + env.trim() + ".properties", false);
        }

        initialized = true;
    }

    public static String getRequiredProperty(String key) {
        ensureInitialized();
        String value = resolveValue(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Cấu hình thiếu: key '" + key + "' không tồn tại trong " +
                            "System properties, env file, hoặc config.properties"
            );
        }
        return value.trim();
    }

    public static String getOptionalProperty(String key, String defaultValue) {
        ensureInitialized();
        String value = resolveValue(key);
        return (value != null && !value.isBlank()) ? value.trim() : defaultValue;
    }

    private static String resolveValue(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) return sysProp.trim();
        return props.getProperty(key);
    }

    private static void loadFile(String path, boolean required) {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                if (required) {
                    throw new RuntimeException(
                            "Không tìm thấy file bắt buộc: '" + path + "'. " +
                                    "Hãy tạo file này trong src/test/resources/"
                    );
                }
                return;
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file '" + path + "': " + e.getMessage(), e);
        }
    }

    private static void ensureInitialized() {
        if (!initialized) init();
    }
}