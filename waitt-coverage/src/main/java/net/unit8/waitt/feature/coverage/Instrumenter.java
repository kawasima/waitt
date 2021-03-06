package net.unit8.waitt.feature.coverage;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author kawasima
 */
@SuppressWarnings("SameParameterValue")
public interface Instrumenter {
    void setIgnoreRegexes(Collection<Pattern> ignoreRegexes);

    byte[] instrumentClassByte(InputStream is)
            throws IOException;

    void setIgnoreTrivial(boolean ignoreTrivial);

    void setIgnoreMethodAnnotations(Set<String> ignoreMethodAnnotations);

    void setFailOnError(boolean failOnError);

    // --Commented out by Inspection (14/07/17 15:42):public void setProjectData(ProjectData projectData);

    void setThreadsafeRigorous(boolean threadsafeRigorous);

    ProjectData getProjectData();
}
