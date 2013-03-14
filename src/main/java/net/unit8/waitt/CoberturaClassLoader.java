package net.unit8.waitt;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.IOUtil;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

/**
 * @author kawasima
 */
@SuppressWarnings("rawtypes")
public class CoberturaClassLoader extends WebappClassLoader {
    private static final Logger logger = Logger.getLogger(CoberturaClassLoader.class);

    public static Set<String> instrumentedPackageNames;
    private Collection ignoreRegexes = new Vector();

    private Collection ignoreBranchesRegexes = new Vector();

    private ProjectData projectData = null;

    public CoberturaClassLoader(ClassLoader parent) {
        super(parent);
        projectData = new ProjectData();
    }

    @SuppressWarnings("unchecked")
    public Class loadClass(final String className, boolean resolve)
            throws ClassNotFoundException {
        Class clazz = findLoadedClass(className);
        if (clazz != null) {
            return clazz;
        }
        if (Iterables.any(instrumentedPackageNames, new Predicate<String>() {
            @Override
            public boolean apply(String pkgName) {
                if (className.startsWith("jp.co."))
                    System.err.println(pkgName + ":" + className);
                return className.startsWith(pkgName);
            }
        })) {
            return defineClass(className, resolve);
        } else {
            return getParent().loadClass(className);
        }
    }

    private Class defineClass(String className, boolean resolve) throws ClassNotFoundException {
        Class clazz;
        String path = className.replace('.', '/') + ".class";;

        InputStream is = parent.getResourceAsStream(path);
        ClassWriter cw;
        ClassInstrumenter cv;
        try {
            ClassReader cr = new ClassReader(is);
            cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cv = new ClassInstrumenter(projectData, cw, ignoreRegexes, ignoreBranchesRegexes);
            cr.accept(cv, 0);
        } catch(Throwable t) {
            throw new ClassNotFoundException(t.getMessage());
        } finally {
            IOUtil.closeInputStream(is);
        }

        if (is != null) {
            clazz = defineClass(className, cw.toByteArray());
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
        return null;
    }

    protected Class defineClass(String className, byte[] bytes) {
        return defineClass(className, bytes, 0, bytes.length);
    }

    public ProjectData getProjectData() {
        return projectData;
    }
}
