package net.unit8.waitt.mojo.fork;

import net.unit8.waitt.api.EmbeddedServer;

import java.io.File;
import java.io.FileInputStream;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * Entry point for the forked JVM process.
 * Reads configuration from a properties file, constructs URLClassLoader
 * for the webapp, loads EmbeddedServer via ServiceLoader, and starts it.
 *
 * @author kawasima
 */
public class ForkedRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: ForkedRunner <config-file>");
            System.exit(1);
        }

        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(args[0])) {
            config.load(fis);
        }

        int port = Integer.parseInt(config.getProperty("port", "8080"));
        String contextPath = config.getProperty("contextPath", "");
        String docBase = config.getProperty("docBase", ".");
        String webappClasspathStr = config.getProperty("webapp.classpath", "");

        List<URL> webappUrls = new ArrayList<URL>();
        if (!webappClasspathStr.isEmpty()) {
            for (String path : webappClasspathStr.split(File.pathSeparator)) {
                if (!path.isEmpty()) {
                    webappUrls.add(new File(path).toURI().toURL());
                }
            }
        }
        URLClassLoader webappClassLoader = new URLClassLoader(
                webappUrls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader()
        );

        ServiceLoader<EmbeddedServer> serviceLoaders = ServiceLoader.load(EmbeddedServer.class);
        Iterator<EmbeddedServer> iter = serviceLoaders.iterator();
        if (!iter.hasNext()) {
            System.err.println("[waitt-fork] ERROR: No EmbeddedServer implementation found on classpath");
            System.exit(1);
        }
        EmbeddedServer server = iter.next();
        System.out.println("[waitt-fork] Using server: " + server.getName());

        if ("/".equals(contextPath)) {
            contextPath = "";
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("[waitt-fork] Shutting down server...");
                server.stop();
            }
        }));

        server.setPort(port);
        server.setBaseDir(".");
        try {
            server.start();
            server.setWebappDecorators(Collections.<net.unit8.waitt.api.WebappDecorator>emptyList());
            server.setMainContext(contextPath, docBase, webappClassLoader);
            System.out.println("[waitt-fork] Server started on port " + port);
            server.await();
        } catch (Exception e) {
            System.err.println("[waitt-fork] Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            server.stop();
            webappClassLoader.close();
        }
    }
}
