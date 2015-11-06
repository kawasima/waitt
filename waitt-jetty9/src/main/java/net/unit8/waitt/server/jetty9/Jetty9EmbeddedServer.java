package net.unit8.waitt.server.jetty9;

import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerStatus;
import net.unit8.waitt.api.WebappDecorator;
import net.unit8.waitt.api.configuration.FilterConfiguration;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Jetty9 embedded server.
 *
 * @author kawasima
 */
public class Jetty9EmbeddedServer implements EmbeddedServer {
    final Server server;
    final WaittHandlerList handlers;
    WebAppContext mainWebapp;
    List<WebappDecorator> decorators;


    public Jetty9EmbeddedServer() {
        server = new Server();
        handlers = new WaittHandlerList();
        server.setHandler(handlers);
    }

    @Override
    public String getName() {
        return "jetty9";
    }

    @Override
    public void setPort(int port) {
        NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
    }

    @Override
    public void setBaseDir(String baseDir) {
    }

    @Override
    public void setMainContext(String contextPath, String baseDir, ClassLoader loader) {
        mainWebapp = addWebapp(contextPath, baseDir, loader, true);
    }

    @Override
    public void addContext(String contextPath, String baseDir, ClassLoader loader) {
        addWebapp(contextPath, baseDir, loader, false);
    }

    @Override
    public void setClassLoaderFactory(ClassLoaderFactory factory) {
        ClassLoaderFactoryHolder.setClassLoaderFactory(factory);
    }

    @Override
    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        try {
            mainWebapp.stop();
            mainWebapp.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServerStatus getStatus() {
        if (server.isRunning()) {
            return ServerStatus.RUNNING;
        } else if (server.isStopped()){
            return ServerStatus.STOPPED;
        } else {
            return ServerStatus.UNKNOWN;
        }
    }

    @Override
    public void await() {
        try {
            server.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setWebappDecorators(List<WebappDecorator> decorators) {
        this.decorators = decorators;
    }

    private WebAppContext addWebapp(String contextPath, String baseDir, ClassLoader loader, boolean mainContext) {
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath(contextPath);
        File warFile = new File(baseDir);
        webapp.setWar(warFile.getAbsolutePath());

        for (URL url : ((ClassRealm) loader).getURLs()) {
            webapp.getMetaData().addContainerResource(Resource.newResource(url));
        }
        webapp.setExtractWAR(true);
        webapp.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        webapp.addBean(new ServletContainerInitializersStarter(webapp), true);
        webapp.setAllowNullPathInfo(false);

        if (mainContext) {
            for (WebappDecorator decorator : decorators) {
                for (FilterConfiguration filterConfig : decorator.getFilterConfigs()) {
                    FilterHolder filterHolder = new FilterHolder();
                    filterHolder.setClassName(filterConfig.getClassName());
                    filterHolder.setName(filterConfig.getName());
                    webapp.addFilter(filterHolder, filterConfig.getUrlPattern()[0], null);
                    ((ClassRealm) loader).importFrom(decorator.getClass().getClassLoader(), filterConfig.getClassName());
                }
            }
        }

        try {
            if (mainContext && ClassLoaderFactoryHolder.getClassLoaderFactory() != null) {
                webapp.setClassLoader(new WebAppClassLoader(ClassLoaderFactoryHolder.getClassLoaderFactory().create(loader), webapp));
            } else {
                webapp.setClassLoader(new WebAppClassLoader(loader, webapp));
            }
            handlers.prependHandler(webapp);
            webapp.start();
            return webapp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<ContainerInitializer> jspInitializers() {
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<ContainerInitializer>();
        initializers.add(initializer);
        return initializers;
    }
}
