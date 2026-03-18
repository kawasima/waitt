package net.unit8.waitt.feature.devtools;

import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
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
    private static final long MIN_INTERVAL = 100L;

    private File outputDirectory;
    private long pollInterval = DEFAULT_POLL_INTERVAL;
    private long quietPeriod = DEFAULT_QUIET_PERIOD;
    private FileSystemPoller poller;
    private Thread pollerThread;

    @Override
    public void config(WebappConfiguration config) {
        outputDirectory = config.getOutputDirectory();
        if (outputDirectory == null && config.getBaseDirectory() != null) {
            outputDirectory = new File(config.getBaseDirectory(), "target/classes");
        }
        for (Feature feature : config.getFeatures()) {
            if ("waitt-devtools".equals(feature.getArtifactId())
                    && "net.unit8.waitt.feature".equals(feature.getGroupId())) {
                Map<String, String> featureConfig = feature.getConfiguration();
                if (featureConfig != null) {
                    pollInterval = parseLong(featureConfig, "poll.interval", DEFAULT_POLL_INTERVAL);
                    quietPeriod = parseLong(featureConfig, "quiet.period", DEFAULT_QUIET_PERIOD);
                }
            }
        }
        pollInterval = Math.max(MIN_INTERVAL, pollInterval);
        quietPeriod = Math.max(0, quietPeriod);
    }

    private long parseLong(Map<String, String> config, String key, long defaultValue) {
        if (!config.containsKey(key)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(config.get(key));
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "Invalid value for " + key + ": " + config.get(key)
                    + ". Using default: " + defaultValue);
            return defaultValue;
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
