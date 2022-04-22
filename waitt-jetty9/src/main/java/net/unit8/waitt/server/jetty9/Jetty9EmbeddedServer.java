package net.unit8.waitt.server.jetty9;

import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerStatus;
import net.unit8.waitt.api.WebappDecorator;
import net.unit8.waitt.api.configuration.FilterConfiguration;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.webapp.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Jetty9 embedded server.
 *
 * @author kawasima
 */
public class Jetty9EmbeddedServer implements EmbeddedServer {
    private final Server server;
    private final WaittHandlerList handlers;
    private WebAppContext mainWebapp;
    private List<WebappDecorator> decorators;

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
        try {
            mainWebapp.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addContext(String contextPath, String baseDir, ClassLoader loader) {
        WebAppContext webapp = addWebapp(contextPath, baseDir, loader, false);
        try {
            webapp.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            if (mainWebapp != null && mainWebapp.isStarted()) {
                mainWebapp.stop();
            }
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
        for (URL url : ((URLClassLoader) loader).getURLs()) {
            try {
                webapp.getMetaData().addContainerResource(new PathResource(new File(url.toURI())));
            } catch (URISyntaxException ignore) {}
        }
        webapp.setExtractWAR(true);
        webapp.setAttribute(AnnotationConfiguration.CONTAINER_INITIALIZERS, jspInitializers());
        webapp.addBean(new ServletContainerInitializersStarter(webapp), true);
        webapp.setAllowNullPathInfo(false);
        webapp.setConfigurations(new Configuration[] {
            new AnnotationConfiguration(),
            new WebInfConfiguration(),
            new WebXmlConfiguration(),
            new MetaInfConfiguration(),
            new FragmentConfiguration(),
            new EnvConfiguration(),
            new PlusConfiguration(),
            new JettyWebXmlConfiguration()
        });

        if (mainContext) {
            for (WebappDecorator decorator : decorators) {
                for (FilterConfiguration filterConfig : decorator.getFilterConfigs()) {
                    FilterHolder filterHolder = new FilterHolder();
                    filterHolder.setClassName(filterConfig.getClassName());
                    filterHolder.setName(filterConfig.getName());
                    webapp.addFilter(filterHolder, filterConfig.getUrlPattern()[0], null);
                }
                for (URL url : ((ClassRealm) decorator.getClass().getClassLoader()).getURLs()) {
                    ((ClassRealm) loader).addURL(url);
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
            Thread.currentThread().setContextClassLoader(loader);
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
