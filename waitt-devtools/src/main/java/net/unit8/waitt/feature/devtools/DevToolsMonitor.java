package net.unit8.waitt.feature.devtools;

import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Development tools monitor that watches for file changes and triggers auto-reload.
 *
 * @author kawasima
 */
public class DevToolsMonitor implements ServerMonitor, ConfigurableFeature {
    private static final Logger LOG = Logger.getLogger(DevToolsMonitor.class.getName());
    private static final long DEFAULT_POLL_INTERVAL = 1000L;
    private static final long DEFAULT_QUIET_PERIOD = 500L;

    private File outputDirectory;
    private long pollInterval = DEFAULT_POLL_INTERVAL;
    private long quietPeriod = DEFAULT_QUIET_PERIOD;
    private FileSystemPoller poller;
    private Thread pollerThread;

    @Override
    public void config(WebappConfiguration config) {
        outputDirectory = config.getOutputDirectory();
        for (Feature feature : config.getFeatures()) {
            if ("waitt-devtools".equals(feature.getArtifactId())
                    && "net.unit8.waitt.feature".equals(feature.getGroupId())) {
                Map<String, String> featureConfig = feature.getConfiguration();
                if (featureConfig != null) {
                    if (featureConfig.containsKey("poll.interval")) {
                        pollInterval = Long.parseLong(featureConfig.get("poll.interval"));
                    }
                    if (featureConfig.containsKey("quiet.period")) {
                        quietPeriod = Long.parseLong(featureConfig.get("quiet.period"));
                    }
                }
            }
        }
    }

    @Override
    public void init(EmbeddedServer server) {
        // no-op
    }

    @Override
    public void start(EmbeddedServer server) {
        if (outputDirectory == null || !outputDirectory.exists()) {
            LOG.warning("Output directory not available. Auto-reload disabled.");
            return;
        }
        poller = new FileSystemPoller(outputDirectory, server, pollInterval, quietPeriod);
        pollerThread = new Thread(poller, "waitt-devtools-poller");
        pollerThread.setDaemon(true);
        pollerThread.start();
        LOG.info("Auto-reload enabled for " + outputDirectory.getAbsolutePath());
    }

    @Override
    public void stop() {
        if (poller != null) {
            poller.stop();
        }
        if (pollerThread != null) {
            pollerThread.interrupt();
            try {
                pollerThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
