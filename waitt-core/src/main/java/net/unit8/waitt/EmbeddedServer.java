package net.unit8.waitt;

import javax.servlet.ServletException;

/**
 * @author kawasima
 */
public interface EmbeddedServer {
    String getName();
    void setPort(int port);
    void setBaseDir(String baseDir);
    void addContext(String contextPath, String baseDir) throws ServletException;
    void setClassLoader(ClassLoader loader);

    void start();
    void stop();
}
