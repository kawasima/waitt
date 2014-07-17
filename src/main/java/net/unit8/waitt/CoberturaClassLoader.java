package net.unit8.waitt;

import net.sourceforge.cobertura.util.IOUtil;
import org.apache.catalina.loader.WebappClassLoader;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
@SuppressWarnings("rawtypes")
public class CoberturaClassLoader extends WebappClassLoader {
    private static final Logger logger = Logger.getLogger(CoberturaClassLoader.class.getName());

    private final Collection<Pattern> ignoreRegexes = new Vector<Pattern>();
    private final Set<String> ignoreMethodAnnotations = new HashSet<String>();

    private Instrumenter instrumenter = null;

    public CoberturaClassLoader(ClassLoader parent) {
        super(parent);
        initInstrumenter();
    }

    private void initInstrumenter() {
        try {
            instrumenter = (Instrumenter) getParent().loadClass("net.unit8.waitt.CoberturaInstrumenterWrapper").newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't find CoberturaInstrumentWrapper.", e);
        }
        instrumenter.setIgnoreRegexes(ignoreRegexes);
        instrumenter.setIgnoreTrivial(false);
        instrumenter
                .setIgnoreMethodAnnotations(ignoreMethodAnnotations);
        instrumenter.setThreadsafeRigorous(false);
        instrumenter.setFailOnError(false);
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
            logger.fine("[loaded] " + className + " from CoberturaClassLoader");
            return defineClass(className, resolve);
        } else {
            return getParent().loadClass(className);
        }
    }

    private Class defineClass(String className, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        String path = className.replace('.', '/') + ".class";
        InputStream is = parent.getResourceAsStream(path);

        byte[] instrumentationResult = null;
        try {
            instrumentationResult = instrumenter.instrumentClassByte(is);
            if (instrumentationResult == null) {
                return getParent().loadClass(className);
            }
        } catch(Throwable t) {
            throw new ClassNotFoundException(t.getMessage() + " from " + parent, t);
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

    public Instrumenter getInstrumenter() {
        return instrumenter;
    }
}
