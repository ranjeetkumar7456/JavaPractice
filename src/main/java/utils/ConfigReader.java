package utils;

/**
 * Simple and Reusable Configuration Reader
 * Use: ConfigReader.getProperty("any.property.key")
 */
public class ConfigReader {

    private static final String DEFAULT_CONFIG_PATH = "src/main/resources/config.properties";

    static {
        PropertyUtils.loadProperties(DEFAULT_CONFIG_PATH);
    }

    /**
     * Get any property value as String
     */
    public static String getProperty(String key) {
        return PropertyUtils.get(key);
    }

    /**
     * Get property with default value if not found
     */
    public static String getProperty(String key, String defaultValue) {
        try {
            return PropertyUtils.get(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get property as integer
     */
    public static int getIntProperty(String key) {
        return PropertyUtils.getInt(key);
    }

    /**
     * Get property as boolean
     */
    public static boolean getBooleanProperty(String key) {
        return PropertyUtils.getBoolean(key);
    }

    /**
     * Get property as long
     */
    public static long getLongProperty(String key) {
        return Long.parseLong(getProperty(key));
    }

    /**
     * Get property as double
     */
    public static double getDoubleProperty(String key) {
        return Double.parseDouble(getProperty(key));
    }

    /**
     * Check if property exists
     */
    public static boolean hasProperty(String key) {
        try {
            PropertyUtils.get(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}