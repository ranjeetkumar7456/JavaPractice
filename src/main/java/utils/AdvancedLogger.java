package utils;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Advanced Logger with colorful console output and file logging
 */
public class AdvancedLogger {

    private static final Logger logger = LogManager.getLogger(AdvancedLogger.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // ANSI color codes for console output
    private static final String RESET = "\033[0m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String PURPLE = "\033[35m";
    private static final String CYAN = "\033[36m";
    private static final String BOLD = "\033[1m";

    public static void info(String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = CYAN + "ℹ️ INFO" + RESET + " [" + timestamp + "] " + message;
        System.out.println(coloredMessage);
        logger.info(message);
    }

    public static void pass(String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = GREEN + "✅ PASS" + RESET + " [" + timestamp + "] " + BOLD + message + RESET;
        System.out.println(coloredMessage);
        logger.info("PASS: " + message);
    }

    public static void fail(String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = RED + "❌ FAIL" + RESET + " [" + timestamp + "] " + BOLD + message + RESET;
        System.out.println(coloredMessage);
        logger.error("FAIL: " + message);
    }

    public static void skip(String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = YELLOW + "⏭️ SKIP" + RESET + " [" + timestamp + "] " + message;
        System.out.println(coloredMessage);
        logger.warn("SKIP: " + message);
    }

    public static void warning(String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = YELLOW + "⚠️ WARN" + RESET + " [" + timestamp + "] " + message;
        System.out.println(coloredMessage);
        logger.warn(message);
    }

    public static void error(String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = RED + "💀 ERROR" + RESET + " [" + timestamp + "] " + BOLD + message + RESET;
        System.out.println(coloredMessage);
        logger.error(message);
    }

    public static void debug(String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = PURPLE + "🐛 DEBUG" + RESET + " [" + timestamp + "] " + message;
        System.out.println(coloredMessage);
        logger.debug(message);
    }

    public static void log(String level, String message) {
        String timestamp = dateFormat.format(new Date());
        String coloredMessage = BLUE + "📝 " + level.toUpperCase() + RESET + " [" + timestamp + "] " + message;
        System.out.println(coloredMessage);

        switch (level.toLowerCase()) {
            case "info": logger.info(message); break;
            case "pass": logger.info("PASS: " + message); break;
            case "fail": logger.error("FAIL: " + message); break;
            case "warn": logger.warn(message); break;
            case "error": logger.error(message); break;
            case "debug": logger.debug(message); break;
            default: logger.info(message);
        }
    }
}
