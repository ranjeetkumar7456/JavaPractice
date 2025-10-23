package ConfigUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * ReportCleanupManager handles cleanup and creation of Extent Reports.
 * Keeps only the latest few reports and deletes the oldest automatically.
 */
public class ReportCleanupManager {

    // Report directory path
    private static final String REPORT_DIR = System.getProperty("user.dir") + File.separator + "extent-reports";

    // Fixed report name (no timestamp)
    private static final String REPORT_NAME = "Amazon_Automation_Extent_Report.html";

    // Max reports to retain
    private static final int MAX_REPORT_COUNT = 8;

    /**
     * Generates a fixed report path and ensures cleanup before creation.
     *
     * @return Full report path for the HTML file
     */
    public static String generateReportPath() {
        ensureReportDirExists();
        cleanupOldReports();

        String reportPath = REPORT_DIR + File.separator + REPORT_NAME;
        System.out.println("📄 Extent Report will be generated at: " + reportPath);
        return reportPath;
    }

    /** Creates the report directory if it doesn’t exist */
    private static void ensureReportDirExists() {
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("📁 Created report directory: " + REPORT_DIR);
            }
        }
    }

    /** Deletes the oldest report if more than MAX_REPORT_COUNT exist */
    public static void cleanupOldReports() {
        File dir = new File(REPORT_DIR);
        File[] reports = dir.listFiles((d, name) -> name.endsWith(".html"));

        if (reports != null && reports.length >= MAX_REPORT_COUNT) {
            Arrays.sort(reports, Comparator.comparingLong(File::lastModified));
            int filesToDelete = reports.length - (MAX_REPORT_COUNT - 1);

            for (int i = 0; i < filesToDelete; i++) {
                boolean deleted = reports[i].delete();
                if (deleted) {
                    System.out.println("🗑️ Deleted old report: " + reports[i].getName());
                } else {
                    System.out.println("⚠️ Failed to delete report: " + reports[i].getName());
                }
            }
        }
    }
}






/*
package ConfigUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

*/
/**
 * ReportCleanupManager handles cleanup and creation of Extent Reports.
 * Keeps only the latest 7 reports and deletes the oldest automatically.
 *//*

public class ReportCleanupManager {

    // Dynamic path for GitHub-safe usage
    private static final String REPORT_DIR = System.getProperty("user.dir") + File.separator + "extent-reports";
    private static final String REPORT_PREFIX = "Amazon_Automation_Report_";
    private static final int MAX_REPORT_COUNT = 8;

    */
/**
     * Generates report path dynamically and ensures cleanup before creating a new one.
     * @return Full report path for the HTML file
     *//*

    public static String generateReportPath() {
        ensureReportDirExists();
        cleanupOldReports();

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        return REPORT_DIR + File.separator + REPORT_PREFIX + timeStamp + ".html";
    }

    */
/** Creates report directory if not already present *//*

    private static void ensureReportDirExists() {
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    */
/** Deletes the oldest report if more than MAX_REPORT_COUNT exist *//*

    public static void cleanupOldReports() {
        File dir = new File(REPORT_DIR);
        File[] reports = dir.listFiles((d, name) -> name.startsWith(REPORT_PREFIX) && name.endsWith(".html"));

        if (reports != null && reports.length >= MAX_REPORT_COUNT) {
            Arrays.sort(reports, Comparator.comparingLong(File::lastModified));
            // Delete oldest until total reports are below limit
            for (int i = 0; i <= reports.length - MAX_REPORT_COUNT; i++) {
                boolean deleted = reports[i].delete();
                if (deleted) {
                    System.out.println("🗑️ Deleted old report: " + reports[i].getName());
                }
            }
        }
    }
}
*/
