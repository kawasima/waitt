package net.unit8.waitt.feature.jacoco;

import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.jacoco.core.JaCoCo;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kawasima
 */
public class JacocoMonitor implements ServerMonitor,ConfigurableFeature {
    private static final Logger LOG = Logger.getLogger(JacocoMonitor.class.getName());
    private static final int MAX_CONSECUTIVE_FAILURES = 3;
    private Set<String> targetPackages;
    private File sourceDirectory;
    ScheduledExecutorService executorService;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    @Override
    public void config(WebappConfiguration config) {
        targetPackages = config.getPackages();
        sourceDirectory = config.getSourceDirectory();
    }

    @Override
    public void init(EmbeddedServer server) {
        final ClassRealm coverageRealm = (ClassRealm) getClass().getClassLoader();
        server.setClassLoaderFactory(new ClassLoaderFactory() {
            @Override
            public ClassLoader create(ClassLoader parent) {
                coverageRealm.setParentClassLoader(parent);
                JacocoClassLoader ccl = JacocoClassLoader.create(coverageRealm);
                ccl.setTargetPackages(targetPackages);
                return ccl;
            }
        });

        LOG.info("JaCoCo offline instrumentation enabled.");
    }

    @Override
    public void start(EmbeddedServer server) {
        File reportDirectory = new File("target/coverage");
        if (!reportDirectory.exists()) {
            reportDirectory.mkdirs();
        }

        server.addContext("/_coverage", reportDirectory.getAbsolutePath(), getClass().getClassLoader());
        executorService = Executors.newScheduledThreadPool(1);

        final ScheduledFuture<?>[] taskHolder = new ScheduledFuture<?>[1];
        taskHolder[0] = executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    ExecutionDataStore executionDataStore = collectExecutionData();
                    final IReportVisitor visitor = createVisitor(Locale.getDefault());
                    visitor.visitInfo(
                            new ArrayList<>(),
                            executionDataStore.getContents());
                    createReport(visitor, executionDataStore);
                    visitor.visitEnd();
                    consecutiveFailures.set(0);
                } catch (Exception ex) {
                    int failures = consecutiveFailures.incrementAndGet();
                    if (failures >= MAX_CONSECUTIVE_FAILURES) {
                        LOG.log(Level.SEVERE, "JaCoCo coverage report generation failed "
                                + failures + " consecutive times. Disabling scheduled reports.", ex);
                        taskHolder[0].cancel(false);
                    } else {
                        LOG.log(Level.WARNING, "Failed to generate coverage report (attempt "
                                + failures + "/" + MAX_CONSECUTIVE_FAILURES + ")", ex);
                    }
                }
            }
        }, 30L, 30L, TimeUnit.SECONDS);
    }

    /**
     * Collect execution data from JaCoCo's Offline runtime via reflection.
     * The Offline class stores probe data in a private RuntimeData field.
     * We access it reflectively because waitt uses offline instrumentation
     * (not -javaagent), so RT.getAgent() is not available.
     */
    private ExecutionDataStore collectExecutionData() throws Exception {
        // Try RT.getAgent() first (works when -javaagent is used)
        try {
            byte[] data = org.jacoco.agent.rt.RT.getAgent().getExecutionData(false);
            ExecFileLoader loader = new ExecFileLoader();
            loader.load(new java.io.ByteArrayInputStream(data));
            return loader.getExecutionDataStore();
        } catch (IllegalStateException e) {
            // Agent not started — fall through to offline approach
        }

        // Offline instrumentation: access RuntimeData via reflection on Offline class.
        // JaCoCo.RUNTIMEPACKAGE resolves the version-specific internal package name.
        Class<?> offlineClass = Class.forName(JaCoCo.RUNTIMEPACKAGE + ".Offline");
        // Call getRuntimeData() which initializes data if needed
        Method getRuntimeData = offlineClass.getDeclaredMethod("getRuntimeData");
        getRuntimeData.setAccessible(true);
        Object runtimeData = getRuntimeData.invoke(null);

        // RuntimeData has a 'store' field of type ExecutionDataStore
        Field storeField = runtimeData.getClass().getDeclaredField("store");
        storeField.setAccessible(true);
        Object internalStore = storeField.get(runtimeData);

        // The internal ExecutionDataStore is a shaded class, so we transfer data
        // by iterating contents and reconstructing with our classpath's types.
        ExecutionDataStore store = new ExecutionDataStore();
        Method getContentsMethod = internalStore.getClass().getMethod("getContents");
        java.util.Collection<?> contents = (java.util.Collection<?>) getContentsMethod.invoke(internalStore);

        for (Object execData : contents) {
            Method getId = execData.getClass().getMethod("getId");
            Method getName = execData.getClass().getMethod("getName");
            Method getProbes = execData.getClass().getMethod("getProbes");
            long id = (Long) getId.invoke(execData);
            String name = (String) getName.invoke(execData);
            boolean[] probes = (boolean[]) getProbes.invoke(execData);
            store.get(id, name, probes.length).merge(
                    new org.jacoco.core.data.ExecutionData(id, name, probes));
        }

        return store;
    }

    void createReport(final IReportGroupVisitor visitor, ExecutionDataStore executionDataStore) throws IOException {
        final BundleCreator creator = new BundleCreator();
        final IBundleCoverage bundle = creator.createBundle(executionDataStore);
        List<File> sourceDirectories = new ArrayList<File>(1);
        sourceDirectories.add(sourceDirectory);
        final SourceFileCollection locator = new SourceFileCollection(sourceDirectories, "UTF-8");

        visitor.visitBundle(bundle, locator);

    }
    IReportVisitor createVisitor(final Locale locale) throws IOException {
        final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();
        final HTMLFormatter htmlFormatter = new HTMLFormatter();
        htmlFormatter.setOutputEncoding("UTF-8");
        htmlFormatter.setLocale(locale);
        visitors.add(htmlFormatter.createVisitor(new FileMultiReportOutput(new File("target/coverage"))));
        return new MultiReportVisitor(visitors);
    }

    @Override
    public void stop() {
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
