package net.unit8.waitt.feature.coverage;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.util.FileFinder;

import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author kawasima
 */
public class ReportGenerator {
    private static final Logger LOG = Logger.getLogger(ReportGenerator.class.getName());

    private final ComplexityCalculator complexity;
    private final FileFinder finder;
    private final ClassLoader classLoader;
    private final File reportDirectory;

    public ReportGenerator(ClassLoader classLoader, CoverageMonitorConfiguration config) {
        this.classLoader = classLoader;
        finder = new FileFinder();
        finder.addSourceDirectory(config.getSourceDirectory().getAbsolutePath());
        complexity = new ComplexityCalculator(finder);
        reportDirectory = config.getCoverageReportDirectory();
    }

    public void report() {
        if (!(classLoader instanceof CoberturaClassLoader)) {
            LOG.warning("CoverageMonitor wasn't loaded from CoberturaClassLoader.");
        }

        ProjectData data = ((CoberturaClassLoader) classLoader).getInstrumenter().getProjectData();
        TouchCollector.applyTouchesOnProjectData(data);
        CoverageDataFileHandler.saveCoverageData(data, CoverageDataFileHandler.getDefaultDataFile());
        try {
            new HTMLReport(data, reportDirectory, finder, complexity, "UTF-8");
        } catch (Exception ignore) {
                /* ignore */
        }
    }
}
