package net.unit8.waitt.api;

/**
 * @author kawasima
 */
public interface EmbeddedServer {
    String getName();
    void setPort(int port);
    void setBaseDir(String baseDir);
    void setMainContext(String contextPath, String baseDir, ClassLoader loader);
    void addContext(String contextPath, String baseDir, ClassLoader loader);
    void setClassLoaderFactory(ClassLoaderFactory factory);
    void start();
    void await();
    void stop();
}
