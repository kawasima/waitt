package net.unit8.waitt.server.glassfish4;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import net.unit8.waitt.api.ClassLoaderFactory;
import net.unit8.waitt.api.EmbeddedServer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;

/**
 *
 * @author kawasima
 */
public class GlassFish4EmbeddedServer implements EmbeddedServer {
    private volatile boolean stopAwait = false;
    private volatile Thread awaitThread = null;
    
    GlassFishProperties glassfishProperties;
    GlassFish glassfish;

    public GlassFish4EmbeddedServer() {
        glassfishProperties  = new GlassFishProperties();
    }
    
    public String getName() {
        return "glassfish4";
    }

    public void setPort(int port) {
        glassfishProperties.setPort("http-listener", port);
    }

    public void setBaseDir(String baseDir) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setMainContext(String contextPath, String baseDir, ClassLoader loader) {
        try {
            System.out.println(glassfishProperties);
            glassfish = GlassFishRuntime.bootstrap().newGlassFish(glassfishProperties);
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
