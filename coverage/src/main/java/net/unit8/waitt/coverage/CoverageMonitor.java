package net.unit8.waitt.coverage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import java.util.logging.Logger;
import net.unit8.waitt.feature.ServerMonitor;
import net.unit8.waitt.ClassLoaderFactory;
import net.unit8.waitt.EmbeddedServer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * @author kawasima
 */
public class CoverageMonitor implements ServerMonitor {
    private static final Logger LOG = Logger.getLogger(CoverageMonitor.class.getName());
    private final CoverageMonitorConfiguration config;
    ExecutorService executorService;

    public CoverageMonitor() {
        Logger logger = Logger.getLogger(CoverageDataFileHandler.class.getName());
        logger.setUseParentHandlers(false);
        config = new CoverageMonitorConfiguration();
    }

    @Override
    public void config(EmbeddedServer server) {
        final ClassRealm coverageRealm = (ClassRealm) this.getClass().getClassLoader();
        server.setClassLoaderFactory(new ClassLoaderFactory() {
            @Override
            public ClassLoader create(ClassLoader parent) {
                coverageRealm.setParentClassLoader(parent);
                return CoberturaClassLoader.create(coverageRealm);
            }
        });
    }
    
    @Override
    public void start(EmbeddedServer server) {
        server.addContext("/coverage", new File("target/coverage").getAbsolutePath());
        executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        CoberturaClassLoader loader = CoberturaClassLoader.getInstance();
                        LOG.info("Start reporting Coverage report");
                        executorService.execute(new ReportGenerator(loader, config));
                        break;
                    } catch (IllegalStateException e) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
            }
        });
    }
    
    @Override
    public void stop() {
        executorService.shutdown();
    }
}
