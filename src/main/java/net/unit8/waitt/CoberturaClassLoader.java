package net.unit8.waitt;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.IOUtil;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
@SuppressWarnings("rawtypes")
public class CoberturaClassLoader extends WebappClassLoader {
    private static final Logger logger = Logger.getLogger(CoberturaClassLoader.class);

    private Collection<Pattern> ignoreRegexes = new Vector<Pattern>();
    private boolean ignoreTrivial = false;
    private Set<String> ignoreMethodAnnotations = new HashSet<String>();
    private boolean threadsafeRigorous = false;
    private boolean failOnError = false;

    private ProjectData projectData = null;
    private Instrumenter instrumenter = null;

    public CoberturaClassLoader(ClassLoader parent) {
        super(parent);
    }

    private void initInstrumenter() {
        try {
            instrumenter = (Instrumenter) getParent().loadClass("net.unit8.waitt.CoberturaInstrumenterWrapper").newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't find CoberturaInstrumentWrapper.", e);
        }
        instrumenter.setIgnoreRegexes(ignoreRegexes);

        File dataFile = CoverageDataFileHandler.getDefaultDataFile();
        projectData = CoverageDataFileHandler.loadCoverageData(dataFile);
        if (projectData == null)
            projectData = new ProjectData();

        instrumenter.setIgnoreTrivial(ignoreTrivial);
        instrumenter
                .setIgnoreMethodAnnotations(ignoreMethodAnnotations);
        instrumenter.setThreadsafeRigorous(threadsafeRigorous);
        instrumenter.setFailOnError(failOnError);
        instrumenter.setProjectData(projectData);
    }
    @SuppressWarnings("unchecked")
    public Class loadClass(final String className, boolean resolve)
            throws ClassNotFoundException {
        Class clazz = findLoadedClass(className);
        if (clazz != null) {
            return clazz;
        }
        Boolean isTargetPackage = false;
        for (String pkgName : TargetPackages.getInstance().get()) {
            isTargetPackage |= className.startsWith(pkgName);
        }
        if (isTargetPackage) {
            return defineClass(className, resolve);
        } else {
            return getParent().loadClass(className);
        }
    }

    private Class defineClass(String className, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        String path = className.replace('.', '/') + ".class";

        if (instrumenter == null) {
            synchronized (this) {
                initInstrumenter();
            }
        }
        InputStream is = parent.getResourceAsStream(path);

        byte[] instrumentationResult = null;
        try {
            instrumentationResult = instrumenter.instrumentClassByte(is);
        } catch(Throwable t) {
            throw new ClassNotFoundException(t.getMessage() + " from " + ((URLClassLoader) parent).getURLs(), t);
        } finally {
            IOUtil.closeInputStream(is);
        }

        if (is != null) {
            clazz = defineClass(className, instrumentationResult);
            if (resolve) {
                resolveClass(clazz);
            }
        }

        return clazz;
    }

    Class defineClass(String className, byte[] bytes) {
        return defineClass(className, bytes, 0, bytes.length);
    }

    public ProjectData getProjectData() {
        return projectData;
    }
}
