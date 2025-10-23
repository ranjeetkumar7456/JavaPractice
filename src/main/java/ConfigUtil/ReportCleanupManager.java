package ConfigUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * ReportCleanupManager handles cleanup and creation of Extent Reports.
 * Keeps only the latest 7 reports and deletes the oldest automatically.
 */
public class ReportCleanupManager {

    // Dynamic path for GitHub-safe usage
    private static final String REPORT_DIR = System.getProperty("user.dir") + File.separator + "extent-reports";
    private static final String REPORT_PREFIX = "Amazon_Automation_Report_";
    private static final int MAX_REPORT_COUNT = 8;

    /**
     * Generates report path dynamically and ensures cleanup before creating a new one.
     * @return Full report path for the HTML file
     */
    public static String generateReportPath() {
        ensureReportDirExists();
        cleanupOldReports();

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        return REPORT_DIR + File.separator + REPORT_PREFIX + timeStamp + ".html";
    }

    /** Creates report directory if not already present */
    private static void ensureReportDirExists() {
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /** Deletes the oldest report if more than MAX_REPORT_COUNT exist */
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
