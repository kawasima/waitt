package net.unit8.waitt.feature.jacoco;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.jacoco.agent.rt.internal_b0d6a23.Agent;
import org.jacoco.agent.rt.internal_b0d6a23.core.runtime.AgentOptions;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

/**
 *
 * @author kawasima
 */
public class JacocoMonitor implements ServerMonitor,ConfigurableFeature {
    private Set<String> targetPackages;
    private File sourceDirectory;
    ExecutorService executorService;

    @Override
    public void config(WebappConfiguration config) {
        targetPackages = config.getPackages();
        sourceDirectory = config.getSourceDirectory();
    }

    @Override
    public void init(EmbeddedServer server) {
        final ClassRealm coverageRealm = (ClassRealm) this.getClass().getClassLoader();
        server.setClassLoaderFactory(new ClassLoaderFactory() {
            @Override
            public ClassLoader create(ClassLoader parent) {
                coverageRealm.setParentClassLoader(parent);
                JacocoClassLoader ccl = JacocoClassLoader.create(coverageRealm);
                ccl.setTargetPackages(targetPackages);
                return ccl;
            }
        });
        Agent.getInstance(new AgentOptions());
    }    

    @Override
    public void start(EmbeddedServer server) {
        File reportDirectory = new File("target/coverage");
        if (!reportDirectory.exists()) {
            reportDirectory.mkdirs();
        }

        server.addContext("/_coverage", reportDirectory.getAbsolutePath(), getClass().getClassLoader());
        executorService = Executors.newCachedThreadPool();
        final ExecFileLoader loader = new ExecFileLoader();
        try {
            loader.load(new File("jacoco.exec"));
        } catch (IOException ignore) {}
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Agent.getInstance().dump(false);
                        
                        loader.load(new File("jacoco.exec"));
                        final IReportVisitor visitor = createVisitor(Locale.getDefault());
                        visitor.visitInfo(
                                loader.getSessionInfoStore().getInfos(),
                                loader.getExecutionDataStore().getContents());
                        createReport(visitor, loader.getExecutionDataStore());
                        visitor.visitEnd();
                    } catch (IOException ex) {
                        // ignore
                    }
                    
                    
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ignore) { /* ignore */ }
                }
            }
        });
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
        executorService.shutdown();
    }

}
