package net.unit8.waitt.embed;

import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.LogListener;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.WebappDecorator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author kawasima
 */
public class Runner {
    private final List<ServerMonitor> serverMonitors = new ArrayList<ServerMonitor>();
    private final List<LogListener> logListeners = new ArrayList<LogListener>();
    private final List<WebappDecorator> webappDecorators = new ArrayList<WebappDecorator>();
    private String contextPath = "";
    private String docBase = ".";
    private int port = 3000;

    public void execute(EmbeddedServer embeddedServer) throws RuntimeException {
        embeddedServer.setPort(port);

        if (contextPath == null || contextPath.equals("/"))
            contextPath = "";
        embeddedServer.setBaseDir(".");

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
        runner.setContextPath(cmd.getOptionValue("path", ""));
        String appdir = cmd.getOptionValue("appdir", ".");
        if (!appdir.startsWith("/")) {
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
