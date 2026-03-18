package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpServer;
import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.feature.admin.routes.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kawasima
 */
public class AdminServer implements ServerMonitor, ConfigurableFeature {
    private static final Logger LOG = Logger.getLogger(AdminServer.class.getName());

    final ExecutorService executorService = new ThreadPoolExecutor(1, 10, 15L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread t = new Thread(runnable);
                    t.setName("admin-server-" + counter.getAndIncrement());
                    if (t.isDaemon()) {
                        t.setDaemon(false);
                    }
                    if (t.getPriority() != 5) {
                        t.setPriority(5);
                    }
                    return t;
                }
            });
    HttpServer adminServer;
    String rrdPath;

    final AdminApplication app = new AdminApplication();
    int adminPort = 1192;

    @Override
    public void config(WebappConfiguration config) {
        app.addRoutes(new CorsAction());
        app.addRoutes(new AppAction(config));
        app.addRoutes(new EnvPropertyAction());
        app.addRoutes(new ThreadDumpAction());
        app.addRoutes(new HeapDumpAction());
        app.addRoutes(new LoggersAction());
        app.addRoutes(new StartupAction());
        app.addRoutes(new RequestLogAction());
        app.addRoutes(new PrometheusAction());

        for (Feature feature : config.getFeatures()) {
            if ("waitt-admin".equals(feature.getArtifactId()) && "net.unit8.waitt.feature".equals(feature.getGroupId())) {
                Map<String, String> featureConfig = feature.getConfiguration();
                if (featureConfig != null) {
                    String portStr = featureConfig.get("admin.port");
                    if (portStr != null) {
                        try {
                            adminPort = Integer.parseInt(portStr);
                        } catch (NumberFormatException e) {
                            LOG.warning("Invalid admin.port value: " + portStr + ", using default port " + adminPort);
                        }
                    }
                }
            }
        }
        rrdPath = "target/rrd/" + config.getApplicationName() + ".rrd";
    }

    @Override
    public void init(EmbeddedServer server) {
        executorService.execute(new MonitoringPost(rrdPath));
    }

    @Override
    public void start(EmbeddedServer server) {
        long startedAt = System.currentTimeMillis();
        app.addRoutes(new ServerAction(server, rrdPath));
        app.addRoutes(new ReloadAction(server));
        Object webappCl = System.getProperties().get("waitt.webapp.classloader");
        ClassLoader appClassLoader = webappCl instanceof ClassLoader ? (ClassLoader) webappCl : Thread.currentThread().getContextClassLoader();
        app.addRoutes(new ClassLoadersAction(appClassLoader));
        app.addRoutes(new DependenciesAction(appClassLoader));
        // Static file handler must be last (fallback for dashboard UI)
        app.addRoutes(new StaticFileAction());
        try {
            adminServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), adminPort), 0);
            adminServer.setExecutor(executorService);
            adminServer.createContext("/", app);
            adminServer.start();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to start an admin server.", ex);
        }
    }

    @Override
    public void stop() {
        if (adminServer != null) {
            adminServer.stop(0);
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
