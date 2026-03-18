package net.unit8.waitt.embed;

import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.LogListener;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.WebappDecorator;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class Runner {
    private final List<ServerMonitor> serverMonitors = new ArrayList<ServerMonitor>();
    private final List<LogListener> logListeners = new ArrayList<LogListener>();
    private final List<WebappDecorator> webappDecorators = new ArrayList<WebappDecorator>();
    private boolean featuresLoaded = false;
    private Handler logHandler;
    private String contextPath = "";
    private String docBase = ".";
    private int port = 3000;

    private void loadFeatures() {
        if (featuresLoaded) {
            return;
        }
        serverMonitors.clear();
        logListeners.clear();
        webappDecorators.clear();

        WebappConfiguration config = new WebappConfiguration();
        config.setBaseDirectory(new File(docBase));
        config.setSourceDirectory(new File(docBase, "src/main/java"));

        for (ServerMonitor monitor : ServiceLoader.load(ServerMonitor.class)) {
            if (monitor instanceof ConfigurableFeature) {
                ((ConfigurableFeature) monitor).config(config);
            }
            serverMonitors.add(monitor);
        }
        for (LogListener listener : ServiceLoader.load(LogListener.class)) {
            if (listener instanceof ConfigurableFeature) {
                ((ConfigurableFeature) listener).config(config);
            }
            logListeners.add(listener);
        }
        for (WebappDecorator decorator : ServiceLoader.load(WebappDecorator.class)) {
            if (decorator instanceof ConfigurableFeature) {
                ((ConfigurableFeature) decorator).config(config);
            }
            webappDecorators.add(decorator);
        }
        featuresLoaded = true;
    }

    private void initLogger() {
        if (logListeners.isEmpty()) {
            return;
        }
        Logger logger = Logger.getLogger("net.unit8.waitt");
        logger.setLevel(Level.ALL);
        if (logHandler != null) {
            logger.removeHandler(logHandler);
        }
        logHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLoggerName() != null && record.getLoggerName().startsWith("sun.awt."))
                    return;
                Level lv = record.getLevel();
                for (LogListener logListener : logListeners) {
                    if (lv.intValue() < Level.INFO.intValue()) {
                        logListener.debug(record.getMessage(), record.getThrown());
                    } else if (lv.equals(Level.INFO)) {
                        logListener.info(record.getMessage(), record.getThrown());
                    } else if (lv.equals(Level.WARNING)) {
                        logListener.warn(record.getMessage(), record.getThrown());
                    } else if (lv.equals(Level.SEVERE)) {
                        logListener.error(record.getMessage(), record.getThrown());
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        logger.addHandler(logHandler);
    }

    public void execute(EmbeddedServer embeddedServer) throws RuntimeException {
        embeddedServer.setPort(port);

        if (contextPath == null || contextPath.equals("/"))
            contextPath = "";
        embeddedServer.setBaseDir(".");

        loadFeatures();
        initLogger();

        try {
            embeddedServer.start();
            for (ServerMonitor serverMonitor : serverMonitors) {
                serverMonitor.init(embeddedServer);
            }
            embeddedServer.setWebappDecorators(webappDecorators);
            embeddedServer.setMainContext(contextPath, docBase, getClass().getClassLoader());

            for (ServerMonitor serverMonitor : serverMonitors) {
                serverMonitor.start(embeddedServer);
            }
            embeddedServer.await();
        } catch (Exception e) {
            throw new RuntimeException("Fail to start server", e);
        } finally {
            for (ServerMonitor serverMonitor : serverMonitors) {
                try {
                    serverMonitor.stop();
                } catch (Exception e) {
                    // log or ignore
                }
            }
            embeddedServer.stop();
        }
    }

    public static void main(String[] args) throws ParseException {
        WaittRunnerOptions options = new WaittRunnerOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        ServiceLoader<EmbeddedServer> serviceLoaders = ServiceLoader.load(EmbeddedServer.class);
        Iterator<EmbeddedServer> iter = serviceLoaders.iterator();
        if (!iter.hasNext()) {
            throw new IllegalArgumentException("Embedded server is not found.");
        }
        Runner runner = new Runner();
        runner.setPort(Integer.parseInt(cmd.getOptionValue("port", "3000")));
        runner.setContextPath(cmd.getOptionValue("prefix", ""));
        String appdir = cmd.getOptionValue("appdir", ".");
        if (!new File(appdir).isAbsolute()) {
            appdir = new File(appdir).getAbsolutePath();
        }
        runner.setDocBase(appdir);

        runner.execute(iter.next());
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
