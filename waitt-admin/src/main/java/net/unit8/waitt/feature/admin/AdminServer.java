package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpServer;
import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.api.observability.RequestTrace;
import net.unit8.waitt.api.observability.TraceStore;
import net.unit8.waitt.feature.admin.json.JSONObject;
import net.unit8.waitt.feature.admin.routes.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
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
    private static final int DEFAULT_LOG_BUFFER_SIZE = 1000;
    private static final long METRICS_INTERVAL_MILLIS = 2000L;

    final ExecutorService executorService = new ThreadPoolExecutor(1, 20, 15L, TimeUnit.SECONDS,
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
    final EventBroadcaster broadcaster = EventBroadcaster.getInstance();
    AdminMetrics metrics;
    LogBuffer logBuffer;
    ConsoleCapture consoleCapture;
    ScheduledExecutorService metricsScheduler;
    int adminPort = 1192;

    @Override
    public void config(WebappConfiguration config) {
        int logBufferSize = DEFAULT_LOG_BUFFER_SIZE;
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
                    String bufStr = featureConfig.get("log.buffer.size");
                    if (bufStr != null) {
                        try {
                            logBufferSize = Integer.parseInt(bufStr);
                        } catch (NumberFormatException e) {
                            LOG.warning("Invalid log.buffer.size value: " + bufStr + ", using default " + logBufferSize);
                        }
                    }
                }
            }
        }

        metrics = new AdminMetrics();
        logBuffer = new LogBuffer(logBufferSize);
        consoleCapture = new ConsoleCapture(logBuffer, broadcaster);

        app.addRoutes(new CorsAction());
        app.addRoutes(new AppAction(config));
        app.addRoutes(new EnvPropertyAction());
        app.addRoutes(new ThreadDumpAction());
        app.addRoutes(new HeapDumpAction());
        app.addRoutes(new LoggersAction());
        app.addRoutes(new StartupAction());
        app.addRoutes(new RequestLogAction());
        app.addRoutes(new PrometheusAction(metrics));
        app.addRoutes(new StreamAction(broadcaster));
        app.addRoutes(new LogsAction(logBuffer));
        app.addRoutes(new TracesAction());

        rrdPath = "target/rrd/" + config.getApplicationName() + ".rrd";
    }

    @Override
    public void init(EmbeddedServer server) {
        executorService.execute(new MonitoringPost(rrdPath));
        // Set up request listener for application request logging and metrics.
        // This runs on the application's request thread (Tomcat Valve / Jetty
        // Handler), so a telemetry hiccup must never break the request.
        server.setRequestListener(new EmbeddedServer.RequestListener() {
            @Override
            public void onRequest(String method, String path, int status, long durationMs) {
                try {
                    RequestLogAction.record(method, path, status, durationMs);
                    metrics.recordHttpRequest(method, status, durationMs);
                } catch (RuntimeException e) {
                    LOG.log(Level.FINE, "Failed to record request telemetry", e);
                }
            }
        });
        // Push completed traces to the dashboard live.
        TraceStore.getInstance().setListener(new TraceStore.TraceListener() {
            @Override
            public void onTraceCompleted(RequestTrace trace) {
                if (broadcaster.hasSubscribers()) {
                    broadcaster.publish("trace", TracesAction.summary(trace).toJSONString());
                }
            }
        });
    }

    @Override
    public void start(EmbeddedServer server) {
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
            return;
        }
        // Only take global side effects (console tee, metrics ticker) once the
        // dashboard is actually reachable.
        consoleCapture.install();
        startMetricsScheduler();
    }

    private void startMetricsScheduler() {
        metricsScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable, "admin-metrics-ticker");
                t.setDaemon(true);
                return t;
            }
        });
        metricsScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!broadcaster.hasSubscribers()) {
                    return;
                }
                try {
                    broadcaster.publish("metrics", metricsSnapshot());
                } catch (RuntimeException e) {
                    LOG.log(Level.FINE, "Failed to publish metrics snapshot", e);
                }
            }
        }, METRICS_INTERVAL_MILLIS, METRICS_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private String metricsSnapshot() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();
        MemoryUsage nonHeap = memory.getNonHeapMemoryUsage();
        JSONObject json = new JSONObject();
        json.put("timestamp", System.currentTimeMillis());
        json.put("heapUsed", heap.getUsed());
        json.put("heapCommitted", heap.getCommitted());
        json.put("heapMax", heap.getMax());
        json.put("nonHeapUsed", nonHeap.getUsed());
        json.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());
        return json.toJSONString();
    }

    @Override
    public void stop() {
        if (consoleCapture != null) {
            consoleCapture.restore();
        }
        if (metricsScheduler != null) {
            metricsScheduler.shutdownNow();
        }
        TraceStore.getInstance().setListener(null);
        broadcaster.shutdown();
        System.getProperties().remove(RequestLogAction.LOG_KEY);
        if (metrics != null) {
            metrics.close();
        }
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
