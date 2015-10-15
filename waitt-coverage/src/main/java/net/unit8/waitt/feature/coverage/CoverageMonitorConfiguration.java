package net.unit8.waitt.feature.coverage;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * @author kawasima
 */
@Data
public class CoverageMonitorConfiguration implements Serializable {
    private File coverageReportDirectory;
    private File sourceDirectory = new File("src/main/java");
    private Set<String> targetPackages = new HashSet<String>();
    private long reportIntervalSeconds = 30L;
}
