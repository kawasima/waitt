package net.unit8.waitt;

import java.io.File;
import java.io.Serializable;

/**
 * @author kawasima
 */
public class CoverageMonitorConfiguration implements Serializable {
    private File coverageReportDirectory;
    private File sourceDirectory = new File("src/main/java");
    private long reportIntervalSeconds = 30L;

    public File getCoverageReportDirectory() {
        return coverageReportDirectory;
    }

    public void setCoverageReportDirectory(File coverageReportDirectory) {
        this.coverageReportDirectory = coverageReportDirectory;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public long getReportIntervalSeconds() {
        return reportIntervalSeconds;
    }

    public void setReportIntervalSeconds(long reportIntervalSeconds) {
        this.reportIntervalSeconds = reportIntervalSeconds;
    }
}
