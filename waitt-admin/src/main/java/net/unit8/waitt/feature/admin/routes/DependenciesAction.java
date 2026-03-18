package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Show dependency libraries (JARs on classpath with Maven coordinates).
 *
 * @author kawasima
 */
public class DependenciesAction implements Route {
    private final ClassLoader appClassLoader;

    public DependenciesAction(ClassLoader appClassLoader) {
        this.appClassLoader = appClassLoader;
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/dependencies".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        List<JSONObject> deps = new ArrayList<JSONObject>();

        ClassLoader cl = appClassLoader;
        while (cl != null) {
            URL[] urls = null;
            try {
                java.lang.reflect.Method getURLs = cl.getClass().getMethod("getURLs");
                Object result = getURLs.invoke(cl);
                if (result instanceof URL[]) {
                    urls = (URL[]) result;
                }
            } catch (Exception ignored) {}

            if (urls == null && cl instanceof java.net.URLClassLoader) {
                urls = ((java.net.URLClassLoader) cl).getURLs();
            }

            if (urls != null) {
                for (URL url : urls) {
                    String path = url.getPath();
                    if (path.endsWith(".jar")) {
                        JSONObject dep = parseJar(path);
                        if (dep != null) {
                            deps.add(dep);
                        }
                    }
                }
            }
            cl = cl.getParent();
        }

        json.put("dependencies", deps);
        ResponseUtils.responseJSON(exchange, json);
    }

    private JSONObject parseJar(String jarPath) {
        JSONObject dep = new JSONObject();
        dep.put("path", jarPath);

        try (JarFile jar = new JarFile(jarPath)) {
            // Try to find pom.properties for Maven coordinates
            java.util.Enumeration<? extends ZipEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith("pom.properties") && entry.getName().startsWith("META-INF/maven/")) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        Properties props = new Properties();
                        props.load(is);
                        dep.put("groupId", props.getProperty("groupId", ""));
                        dep.put("artifactId", props.getProperty("artifactId", ""));
                        dep.put("version", props.getProperty("version", ""));
                        return dep;
                    }
                }
            }
            // No pom.properties, extract name from filename
            String name = jarPath.substring(jarPath.lastIndexOf('/') + 1);
            dep.put("artifactId", name.replaceAll("-[0-9].*\\.jar$", ""));
            dep.put("version", extractVersion(name));
        } catch (IOException e) {
            String name = jarPath.substring(jarPath.lastIndexOf('/') + 1);
            dep.put("artifactId", name);
        }
        return dep;
    }

    private String extractVersion(String filename) {
        // Extract version from filename like "commons-io-2.14.0.jar"
        String noExt = filename.replace(".jar", "");
        int lastDash = noExt.length() - 1;
        while (lastDash > 0 && (Character.isDigit(noExt.charAt(lastDash)) || noExt.charAt(lastDash) == '.' || noExt.charAt(lastDash) == '-')) {
            lastDash--;
        }
        if (lastDash < noExt.length() - 1 && noExt.charAt(lastDash) == '-') {
            return noExt.substring(lastDash + 1);
        }
        return "";
    }
}
