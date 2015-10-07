package net.unit8.waitt;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.util.FileFinder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class CoverageMonitor implements Runnable {
    private static final Logger logger = Logger.getLogger(CoverageMonitor.class.getName());

    private final ComplexityCalculator complexity;
    private final FileFinder finder;
    private final ClassLoader classLoader;
    private final CoverageMonitorConfiguration config;

    public CoverageMonitor(ClassLoader classLoader, CoverageMonitorConfiguration config) {
        this.classLoader = classLoader;
        this.config = config;

        finder = new FileFinder();
        finder.addSourceDirectory(config.getSourceDirectory().getAbsolutePath());
        complexity = new ComplexityCalculator(finder);

        Logger logger = Logger.getLogger(CoverageDataFileHandler.class.getName());
        logger.setUseParentHandlers(false);
    }

    @Override
    public void run() {
        while(true) {
            if (!(classLoader instanceof CoberturaClassLoader)) {
                logger.warning("CoverageMonitor wasn't loaded from CoberturaClassLoader.");
                break;
            }

            ProjectData data = ((CoberturaClassLoader) classLoader).getInstrumenter().getProjectData();
            TouchCollector.applyTouchesOnProjectData(data);
            CoverageDataFileHandler.saveCoverageData(data, CoverageDataFileHandler.getDefaultDataFile());
            try {
                new HTMLReport(data, config.getCoverageReportDirectory(), finder, complexity, "UTF-8");
            } catch (Exception ignore) {
                    /* ignore */
            }

            try {
                TimeUnit.SECONDS.sleep(config.getReportIntervalSeconds());
            } catch (InterruptedException ignore) { /* ignore */ }
        }
    }

}
