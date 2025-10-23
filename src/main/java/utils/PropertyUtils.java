package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * PropertyUtils - Utility class to read properties file
 */
public class PropertyUtils {

    private static final Properties properties = new Properties();

    /**
     * Load properties from file
     */
    public static void loadProperties(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
            AdvancedLogger.info("✅ Properties loaded from: " + filePath);
        } catch (IOException e) {
            AdvancedLogger.error("❌ Failed to load properties file: " + filePath);
            throw new RuntimeException("Failed to load properties file: " + filePath, e);
        }
    }

    /**
     * Get property value as String
     */
    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config file");
        }
        return value.trim();
    }

    /**
     * Get property value as int
     */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Get property value as boolean
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Get property value with default value
     */
    public static String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? value.trim() : defaultValue;
    }
}