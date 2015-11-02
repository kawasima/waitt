package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpServer;
import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.feature.admin.routes.AppAction;
import net.unit8.waitt.feature.admin.routes.ReloadAction;
import net.unit8.waitt.feature.admin.routes.ServerAction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kawasima
 */
public class AdminServer implements ServerMonitor, ConfigurableFeature {
    private static final Logger LOG = Logger.getLogger(AdminServer.class.getName());

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    HttpServer adminServer;
    String rrdPath;

    AdminApplication app = new AdminApplication();
    int adminPort = 1192;
    private long startedAt;

    @Override
    public void config(WebappConfiguration config) {
        app.addRoutes(new AppAction(config));
        for (Feature feature : config.getFeatures()) {
            if ("waitt-admin".equals(feature.getArtifactId()) && "net.unit8.waitt.feature".equals(feature.getGroupId())) {
                Map<String, String> featureConfig = feature.getConfiguration();
                if (featureConfig != null) {
                    String portStr = featureConfig.get("admin.port");
                    if (portStr != null) {
                        adminPort = Integer.parseInt(portStr);
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
        startedAt = System.currentTimeMillis();
        app.addRoutes(new ServerAction(server, rrdPath));
        app.addRoutes(new ReloadAction(server));
        try {
            adminServer = HttpServer.create(new InetSocketAddress(adminPort), 0);
            adminServer.createContext("/", app);
            adminServer.start();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to start an admin server.", ex);
        }
    }

    @Override
    public void stop() {
    }

}
