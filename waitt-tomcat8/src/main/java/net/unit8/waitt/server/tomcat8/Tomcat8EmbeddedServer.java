package net.unit8.waitt.server.tomcat8;

import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerStatus;
import net.unit8.waitt.api.WebappDecorator;
import net.unit8.waitt.api.configuration.FilterConfiguration;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tomcat8 server.
 *
 * @author kawasima
 */
public class Tomcat8EmbeddedServer implements EmbeddedServer {
    final Tomcat tomcat;
    Context context;
    List<WebappDecorator> decorators;

    public Tomcat8EmbeddedServer() {
        tomcat = new Tomcat();
        if (tomcat.getHost() instanceof StandardHost) {
            StandardHost host = (StandardHost) tomcat.getHost();
            host.setUnpackWARs(true);
            File appBase = new File("target/tomcat8/webapps");
            if (!appBase.exists()) appBase.mkdirs();
            host.setAppBase(appBase.getAbsolutePath());

            File workDir = new File("target/tomcat8/work");
            if (!workDir.exists()) workDir.mkdirs();
            host.setWorkDir(workDir.getAbsolutePath());
        }
    }

    public String getName() {
        return "tomcat8";
    }

    public void setPort(int port) {
        tomcat.setPort(port);
    }

    public void setBaseDir(String baseDir) {
    }

    public void setClassLoaderFactory(ClassLoaderFactory factory) {
        ClassLoaderFactoryHolder.setClassLoaderFactory(factory);
    }

    public void setMainContext(String contextPath, String docBase, ClassLoader classLoader) {
        File appBaseDir = new File(docBase);
        if (!appBaseDir.exists()) {
            if (!appBaseDir.mkdirs()) {
                throw new IllegalStateException("Can't create appBase:" + docBase);
            }
        }
        context = addWebapp(contextPath, docBase, classLoader, true);
        context.setSessionCookieDomain(null);
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default1");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        context.addChild(defaultServlet);
        context.addServletMapping("/", "default1");
        context.addWelcomeFile("index.html");
    }

    public void addContext(String contextPath, String docBase, ClassLoader classLoader) {
        Context context = addWebapp(contextPath, docBase, classLoader, false);
        context.setSessionCookieDomain(null);
        Wrapper defaultServlet = context.createWrapper();

        defaultServlet.setName("default1");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);
        context.addChild(defaultServlet);
        context.addServletMapping("/", "default1");
        context.addWelcomeFile("index.html");
    }

    @Override
    public void start() {
        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);
        server.setParentClassLoader(getClass().getClassLoader());
        tomcat.getConnector().setURIEncoding("UTF-8");
        tomcat.getConnector().setUseBodyEncodingForURI(true);
        try {
            server.start();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void reload() {
        context.reload();
    }

    @Override
    public ServerStatus getStatus() {
        switch(tomcat.getServer().getState()) {
            case STARTED:
                return ServerStatus.RUNNING;
            case STOPPED:
                return ServerStatus.STOPPED;
            default:
                return ServerStatus.UNKNOWN;
        }
    }

    @Override
    public void await() {
        tomcat.getServer().await();
    }

    @Override
    public void stop() {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void setWebappDecorators(List<WebappDecorator> decorators) {
        this.decorators = decorators;
    }

    private Context addWebapp(String contextPath, String appBase, ClassLoader loader, boolean mainContext) {
        Context context;
        String contextClass = ((StandardHost) tomcat.getHost()).getContextClass();
        try {
            context = (Context) Class.forName(contextClass).getConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        if (mainContext) {
            Set<URL> decoratorUrls = new HashSet<URL>();

            for (WebappDecorator decorator : decorators) {
                for (FilterConfiguration filterConfig : decorator.getFilterConfigs()) {
                    FilterDef filterDef = new FilterDef();
                    filterDef.setFilterClass(filterConfig.getClassName());
                    filterDef.setFilterName(filterConfig.getName());
                    context.addFilterDef(filterDef);
                    FilterMap filterMap = new FilterMap();
                    filterMap.setFilterName(filterConfig.getName());
                    for (String urlPattern : filterConfig.getUrlPattern()) {
                        filterMap.addURLPattern(urlPattern);
                    }
                    context.addFilterMap(filterMap);

                    for (URL url : ((URLClassLoader) decorator.getClass().getClassLoader()).getURLs()) {
                        decoratorUrls.add(url);
                    }
                }
            }
            if (!decoratorUrls.isEmpty()) {
                loader = new URLClassLoader(decoratorUrls.toArray(new URL[decoratorUrls.size()]), loader);
            }

        }

        if (loader != null) {
            final WebappLoader webappLoader = new WebappLoader(loader);
            if (mainContext && ClassLoaderFactoryHolder.getClassLoaderFactory() != null) {
                webappLoader.setLoaderClass("net.unit8.waitt.server.tomcat8.Tomcat8WebappClassLoaderWrapper");
                JarScanner jarScanner = new ClassRealmJarScanner();
                context.setJarScanner(jarScanner);
            }
            webappLoader.setDelegate(true);
            context.setLoader(webappLoader);
        }
        ContextConfig config = new ContextConfig();
        context.setPath(contextPath);
        context.setAddWebinfClassesResources(true);
        context.setDocBase(appBase);
        context.addLifecycleListener(new Tomcat.DefaultWebXmlListener());
        context.setConfigFile(null);
        context.addLifecycleListener(config);
        config.setDefaultWebXml(Constants.NoDefaultWebXml);
        tomcat.getHost().addChild(context);
        return context;
    }
}
