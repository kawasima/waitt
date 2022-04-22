package net.unit8.waitt.server.payara;

import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerStatus;
import net.unit8.waitt.api.WebappDecorator;
import org.glassfish.embeddable.*;
import org.glassfish.embeddable.archive.ScatteredArchive;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 *
 * @author kawasima
 */
public class PayaraEmbeddedServer implements EmbeddedServer {
    private volatile boolean stopAwait = false;
    private volatile Thread awaitThread = null;
    private int port;
    
    BootstrapProperties glassfishProperties;
    GlassFish glassfish;

    public PayaraEmbeddedServer() {
        glassfishProperties  = new BootstrapProperties();
    }
    
    public String getName() {
        return "glassfish4";
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBaseDir(String baseDir) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setWebappDecorators(List<WebappDecorator> decorators) {

    }

    public void setMainContext(String contextPath, String baseDir, ClassLoader loader) {
        try {
            System.out.println(glassfishProperties);
            GlassFishRuntime runtime = GlassFishRuntime.bootstrap(glassfishProperties);
            GlassFishProperties properties = new GlassFishProperties();
            properties.setPort("0.0.0.0", port);
            glassfish = runtime.newGlassFish(properties);
            glassfish.start();
            ScatteredArchive archive = new ScatteredArchive("", ScatteredArchive.Type.WAR, new File(baseDir));
            for (URL url : ((URLClassLoader)loader).getURLs()) {
                System.out.println("classpath=" + new File(url.toURI()));
                archive.addClassPath(new File(url.toURI()));
            }
            glassfish.getDeployer().deploy(archive.toURI());
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void addContext(String contextPath, String baseDir, ClassLoader loader) {

    }

    public void setClassLoaderFactory(ClassLoaderFactory factory) {

    }

    public void start() {
        /*
        try {
            glassfish.start();
        } catch (GlassFishException ex) {
            throw new IllegalStateException(ex);
        }
        */
    }

    public void reload() {

    }

    public ServerStatus getStatus() {
        return null;
    }

    public void await() {
        try {
            awaitThread = Thread.currentThread();
            while(!stopAwait) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    // continue and check the flag
                }
            }
        } finally {
            awaitThread = null;
        }
    }

    public void stop() {
        try {
            glassfish.stop();
        } catch (GlassFishException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
}
