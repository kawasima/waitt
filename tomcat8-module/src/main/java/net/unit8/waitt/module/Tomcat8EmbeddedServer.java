package net.unit8.waitt.module;

import net.unit8.waitt.EmbeddedServer;
import org.apache.catalina.*;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;
import net.unit8.waitt.ClassLoaderFactory;

/**
 * Tomcat8 server.
 *
 * @author kawasima
 */
public class Tomcat8EmbeddedServer implements EmbeddedServer {
    Tomcat tomcat;
    ClassLoader classLoader;
    String webappLoaderName = null;

    public Tomcat8EmbeddedServer() {
        tomcat = new Tomcat();
        ((StandardHost)tomcat.getHost()).setUnpackWARs(false);
        System.setProperty("catalina.home", ".");
        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);
        server.setParentClassLoader(getClass().getClassLoader());
        tomcat.getConnector().setURIEncoding("UTF-8");
        tomcat.getConnector().setUseBodyEncodingForURI(true);
    }

    public String getName() {
        return "tomcat8";
    }

    public void setPort(int port) {
        tomcat.setPort(port);
    }

    public void setBaseDir(String baseDir) {
        tomcat.setBaseDir(baseDir);
    }

    public void setClassLoaderFactory(ClassLoaderFactory factory) {
        ClassLoaderFactoryHolder.setClassLoaderFactory(factory);
    }
    
    public void setMainContext(String contextPath, String appBase, ClassLoader classLoader) {
        File appBaseDir = new File(appBase);
        if (!appBaseDir.exists()) {
            if (!appBaseDir.mkdirs()) {
                throw new IllegalStateException("Can't create appBase:" + appBase);
            }
        }
        tomcat.getHost().setAppBase(appBase);
        Context context = null;
        try {
            context = tomcat.addWebapp(contextPath, appBase);
        } catch (ServletException e) {
            throw new IllegalStateException(e);
        }
        final WebappLoader webappLoader = new WebappLoader(classLoader);
        if (ClassLoaderFactoryHolder.getClassLoaderFactory() != null) {
            webappLoader.setLoaderClass("net.unit8.waitt.module.Tomcat8WebappClassLoaderWrapper");
        }
        
        webappLoader.setDelegate(true); // TODO use a mojo setting.
        context.setLoader(webappLoader);
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
    
    public void addContext(String contextPath, String docBase) {
        Context context = null;
        try {
            context = tomcat.addWebapp(contextPath, docBase);
        } catch (ServletException e) {
            throw new IllegalStateException(e);
        }
        
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

    public void start() {
        Server server = tomcat.getServer();

        server.addLifecycleListener(new LifecycleListener() {
            public void lifecycleEvent(LifecycleEvent event) {
                if (event.getType().equals(Lifecycle.BEFORE_STOP_EVENT)) {
//                    executorService.shutdownNow();
  //                  getLog().info("Stop monitoring threads.");
                }
            }
        });
        try {
            server.start();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public void await() {
        tomcat.getServer().await();
    }

    public void stop() {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            throw new IllegalStateException(e);
        }
    }
}
