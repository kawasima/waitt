package net.unit8.waitt;

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
    public void setIgnoreRegexes(Collection<Pattern> ignoreRegexes);

    public byte[] instrumentClassByte(InputStream is)
            throws IOException;

    public void setIgnoreTrivial(boolean ignoreTrivial);

    public void setIgnoreMethodAnnotations(Set<String> ignoreMethodAnnotations);

    public void setFailOnError(boolean failOnError);

    // --Commented out by Inspection (14/07/17 15:42):public void setProjectData(ProjectData projectData);

    public void setThreadsafeRigorous(boolean threadsafeRigorous);

    public ProjectData getProjectData();
}
