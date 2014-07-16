package net.unit8.waitt;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.util.FileFinder;
import org.apache.catalina.loader.WebappLoader;

import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class CoverageMonitor implements Runnable {
    private static final Logger logger = Logger.getLogger(CoverageMonitor.class.getName());

    private ComplexityCalculator complexity;
    private FileFinder finder;
    private WebappLoader webappLoader;
    private CoverageMonitorConfiguration config;

    public CoverageMonitor(WebappLoader webappLoader, CoverageMonitorConfiguration config) {
        this.webappLoader = webappLoader;
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
            ClassLoader cl = webappLoader.getClassLoader();
            if (!(cl instanceof CoberturaClassLoader)) {
                logger.warning("CoverageMonitor wasn't loaded from CoberturaClassLoader.");
                break;
            }

            ProjectData data = ((CoberturaClassLoader)cl).getInstrumenter().getProjectData();
            TouchCollector.applyTouchesOnProjectData(data);
            CoverageDataFileHandler.saveCoverageData(data, CoverageDataFileHandler.getDefaultDataFile());
            try {
                new HTMLReport(data, config.getCoverageReportDirectory(), finder, complexity, "UTF-8");
            } catch (Exception ignore) {
                    /* ignore */
            }

            try {
                Thread.sleep(config.getReportIntervalSeconds() * 1000);
            } catch (InterruptedException ignore) { /* ignore */ }
        }
    }

}
