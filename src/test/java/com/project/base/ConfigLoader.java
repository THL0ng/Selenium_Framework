package com.project.base;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties prop = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config/config.properties")) {
            if (input == null) throw new RuntimeException("Không tìm thấy file config/config.properties");
            prop.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file cấu hình: " + e.getMessage());
        }
    }

    public static String getRequiredProperty(String key) {
        String value = prop.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("CẤU HÌNH THIẾU: Key '" + key + "' không tồn tại!");
        }
        return value;
    }

    public static String getOptionalProperty(String key, String defaultValue) {
        String value = prop.getProperty(key);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }
}