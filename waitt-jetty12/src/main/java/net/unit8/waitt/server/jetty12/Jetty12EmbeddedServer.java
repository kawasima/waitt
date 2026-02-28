package net.unit8.waitt.server.jetty12;

import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerStatus;
import net.unit8.waitt.api.WebappDecorator;
import net.unit8.waitt.api.configuration.FilterConfiguration;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.jetty.ee10.annotations.AnnotationConfiguration;
import org.eclipse.jetty.ee10.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.ee10.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.webapp.Configuration;
import org.eclipse.jetty.ee10.webapp.FragmentConfiguration;
import org.eclipse.jetty.ee10.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.ee10.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee.webapp.CachingWebAppClassLoader;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.ee10.webapp.WebInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebXmlConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jetty 12 embedded server (EE10 / jakarta.servlet).
 *
 * @author kawasima
 */
public class Jetty12EmbeddedServer implements EmbeddedServer {
    private static final Logger LOG = Logger.getLogger(Jetty12EmbeddedServer.class.getName());

    private final Server server;
    private final ContextHandlerCollection handlers;
    private WebAppContext mainWebapp;
    private List<WebappDecorator> decorators;
    private boolean started = false;

    public Jetty12EmbeddedServer() {
        server = new Server();
        handlers = new ContextHandlerCollection();
        server.setHandler(handlers);
    }

    @Override
    public String getName() {
        return "jetty12";
    }

    @Override
    public void setPort(int port) {
        ServerConnector connector = new ServerConnector(server);
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
        File base = new File(baseDir);
        // WAR file → deploy as a full webapp; plain directory → serve as static files
        if (base.isFile() && baseDir.endsWith(".war")) {
            addWebapp(contextPath, baseDir, loader, false);
        } else {
            if (!base.exists()) {
                base.mkdirs();
            }
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setBaseResource(ResourceFactory.of(resourceHandler).newResource(base.toPath()));
            resourceHandler.setDirAllowed(true);
            ContextHandler contextHandler = new ContextHandler(contextPath);
            contextHandler.setHandler(resourceHandler);
            handlers.addHandler(contextHandler);
        }
    }

    @Override
    public void setClassLoaderFactory(ClassLoaderFactory factory) {
        ClassLoaderFactoryHolder.setClassLoaderFactory(factory);
    }

    @Override
    public void start() {
        // Deferred: actual server start happens in await() after all contexts are registered.
        // AbstractRunMojo calls start() before setMainContext(), so we cannot start here.
        started = true;
    }

    private void doStart() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        if (mainWebapp == null) {
            throw new IllegalStateException("Main context has not been set");
        }
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
        } else if (started && !server.isRunning()) {
            return ServerStatus.RUNNING; // started flag set, server about to run
        } else if (server.isStopped()) {
            return ServerStatus.STOPPED;
        } else {
            return ServerStatus.UNKNOWN;
        }
    }

    @Override
    public void await() {
        if (!server.isRunning()) {
            doStart();
        }
        try {
            server.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

        if (loader instanceof URLClassLoader) {
            ResourceFactory resourceFactory = ResourceFactory.of(webapp);
            for (URL url : ((URLClassLoader) loader).getURLs()) {
                try {
                    Path path = Path.of(url.toURI());
                    Resource resource = resourceFactory.newResource(path);
                    webapp.getMetaData().addContainerResource(resource);
                } catch (URISyntaxException e) {
                    LOG.log(Level.WARNING, "Invalid classpath URL: " + url, e);
                }
            }
        }

        webapp.setExtractWAR(true);
        webapp.setThrowUnavailableOnStartupException(true);
        // Disable the default descriptor: its servlets/listeners (DefaultServlet, IntrospectorCleaner)
        // cannot be loaded via the ClassRealm-based classloader hierarchy used by waitt.
        webapp.setDefaultsDescriptor(null);
        // Let AnnotationConfiguration discover JettyJasperInitializer via ServiceLoader
        webapp.setAttribute(
                "org.eclipse.jetty.containerInitializerOrder",
                "org.eclipse.jetty.ee10.apache.jsp.JettyJasperInitializer,*");
        webapp.setConfigurations(new Configuration[]{
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
                    for (String urlPattern : filterConfig.getUrlPatterns()) {
                        webapp.addFilter(filterHolder, urlPattern, null);
                    }
                }
                if (decorator.getClass().getClassLoader() instanceof ClassRealm && loader instanceof ClassRealm) {
                    for (URL url : ((ClassRealm) decorator.getClass().getClassLoader()).getURLs()) {
                        ((ClassRealm) loader).addURL(url);
                    }
                }
            }
        }

        try {
            if (mainContext && ClassLoaderFactoryHolder.getClassLoaderFactory() != null) {
                webapp.setClassLoader(new CachingWebAppClassLoader(
                        ClassLoaderFactoryHolder.getClassLoaderFactory().create(loader), webapp));
            } else {
                webapp.setClassLoader(new CachingWebAppClassLoader(loader, webapp));
            }
            handlers.addHandler(webapp);
            Thread.currentThread().setContextClassLoader(loader);
            return webapp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
