package net.unit8.waitt;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.instrument.CoberturaInstrumenter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class CoberturaInstrumenterWrapper extends CoberturaInstrumenter implements Instrumenter {
    private static final Logger logger = Logger.getLogger(CoberturaInstrumenterWrapper.class.getName());
    private ProjectData projectData;

    public CoberturaInstrumenterWrapper() {
        File dataFile = CoverageDataFileHandler.getDefaultDataFile();
        if (dataFile.exists()) {
            setProjectData(CoverageDataFileHandler.loadCoverageData(dataFile));
        }

        if (getProjectData() == null) {
            setProjectData(new ProjectData());
        }
    }

    public byte[] instrumentClassByte(InputStream is)
            throws IOException {
        CoberturaInstrumenter.InstrumentationResult result = super.instrumentClass(is);
        logger.fine("Instrumented:" + result.getClassName());
        return result.getContent();
    }

    @Override
    public void setProjectData(ProjectData projectData) {
        super.setProjectData(projectData);
        this.projectData = projectData;
    }

    public ProjectData getProjectData() {
        return projectData;
    }
}
