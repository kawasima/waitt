package net.unit8.waitt.feature.coverage;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.util.FileFinder;

/**
 *
 * @author kawasima
 */
public class ReportGenerator implements Runnable {
    private static final Logger LOG = Logger.getLogger(ReportGenerator.class.getName());
    
    private final ComplexityCalculator complexity;
    private final FileFinder finder;
    private final ClassLoader classLoader;
    private final File reportDirectory;
    private final long reportInterval;



    public ReportGenerator(ClassLoader classLoader, CoverageMonitorConfiguration config) {
        this.classLoader = classLoader;
        finder = new FileFinder();
        finder.addSourceDirectory(config.getSourceDirectory().getAbsolutePath());
        complexity = new ComplexityCalculator(finder);
        reportDirectory = config.getCoverageReportDirectory();
        reportInterval = config.getReportIntervalSeconds();
    }
    
    @Override
    public void run() {
        while(true) {
            if (!(classLoader instanceof CoberturaClassLoader)) {
                LOG.warning("CoverageMonitor wasn't loaded from CoberturaClassLoader.");
                break;
            }

            ProjectData data = ((CoberturaClassLoader) classLoader).getInstrumenter().getProjectData();
            TouchCollector.applyTouchesOnProjectData(data);
            CoverageDataFileHandler.saveCoverageData(data, CoverageDataFileHandler.getDefaultDataFile());
            try {
                new HTMLReport(data, reportDirectory, finder, complexity, "UTF-8");
            } catch (Exception ignore) {
                    /* ignore */
            }

            try {
                TimeUnit.SECONDS.sleep(reportInterval);
            } catch (InterruptedException ignore) { /* ignore */ }
        }
    }

    
}
