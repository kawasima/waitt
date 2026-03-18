package net.unit8.waitt.feature.devtools;

import net.unit8.waitt.api.EmbeddedServer;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Polls a directory for file changes and triggers server reload.
 *
 * @author kawasima
 */
class FileSystemPoller implements Runnable {
    private static final Logger LOG = Logger.getLogger(FileSystemPoller.class.getName());

    private final File directory;
    private final EmbeddedServer server;
    private final long pollInterval;
    private final long quietPeriod;
    private volatile boolean running = true;

    FileSystemPoller(File directory, EmbeddedServer server, long pollInterval, long quietPeriod) {
        this.directory = directory;
        this.server = server;
        this.pollInterval = pollInterval;
        this.quietPeriod = quietPeriod;
    }

    @Override
    public void run() {
        LOG.info("Watching for changes in " + directory.getAbsolutePath()
                + " (poll=" + pollInterval + "ms, quiet=" + quietPeriod + "ms)");
        DirectorySnapshot previousSnapshot = new DirectorySnapshot(directory);

        while (running) {
            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            DirectorySnapshot currentSnapshot = new DirectorySnapshot(directory);
            if (currentSnapshot.hasChangedFrom(previousSnapshot)) {
                LOG.info("Changes detected, waiting for quiet period...");
                if (waitForQuietPeriod()) {
                    LOG.info("Triggering reload...");
                    try {
                        server.reload();
                        LOG.info("Reload completed.");
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Reload failed", e);
                    }
                }
                previousSnapshot = new DirectorySnapshot(directory);
            }
        }
    }

    private boolean waitForQuietPeriod() {
        DirectorySnapshot snapshot = new DirectorySnapshot(directory);
        long deadline = System.currentTimeMillis() + quietPeriod;
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            DirectorySnapshot current = new DirectorySnapshot(directory);
            if (current.hasChangedFrom(snapshot)) {
                // More changes came in, reset the quiet period
                snapshot = current;
                deadline = System.currentTimeMillis() + quietPeriod;
            }
        }
        return running;
    }

    void stop() {
        running = false;
    }
}
