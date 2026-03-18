package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Show ClassLoader hierarchy and loaded URLs.
 *
 * @author kawasima
 */
public class ClassLoadersAction implements Route {
    private final ClassLoader appClassLoader;

    public ClassLoadersAction(ClassLoader appClassLoader) {
        this.appClassLoader = appClassLoader;
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/classloaders".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        List<JSONObject> chain = new ArrayList<JSONObject>();

        ClassLoader cl = appClassLoader;
        while (cl != null) {
            JSONObject entry = new JSONObject();
            entry.put("name", cl.getClass().getName());
            entry.put("identity", cl.toString());

            List<String> urls = new ArrayList<String>();
            if (cl instanceof java.net.URLClassLoader) {
                for (URL url : ((java.net.URLClassLoader) cl).getURLs()) {
                    urls.add(url.toString());
                }
            }
            try {
                // Try ClassRealm.getURLs() via reflection
                java.lang.reflect.Method getURLs = cl.getClass().getMethod("getURLs");
                Object result = getURLs.invoke(cl);
                if (result instanceof URL[]) {
                    urls.clear();
                    for (URL url : (URL[]) result) {
                        urls.add(url.toString());
                    }
                }
            } catch (Exception ignored) {
                // Not a ClassRealm or URLClassLoader subclass
            }
            entry.put("urls", urls);
            chain.add(entry);
            cl = cl.getParent();
        }

        json.put("classLoaders", chain);
        ResponseUtils.responseJSON(exchange, json);
    }
}
