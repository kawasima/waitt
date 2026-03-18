package net.unit8.waitt.feature.dashboard;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.unit8.waitt.feature.dashboard.route.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kawasima
 */
public class DashboardApplication extends HttpServlet {
    private final AdminConfig adminConfig = new AdminConfig();
    private final Map<String, Route> getRoutes = new HashMap<>();
    private final Map<String, Route> postRoutes = new HashMap<>();

    @Override
    public void init() throws ServletException {
        adminConfig.read();
        getRoutes.put("/application", new ApplicationRoute(adminConfig));
        getRoutes.put("/server", new ServerRoute(adminConfig));
        getRoutes.put("/env", new EnvPropertyRoute());
        getRoutes.put("/heap", new HeapDumpRoute());
        getRoutes.put("/thread", new ThreadDumpRoute());
        getRoutes.put("/prometheus", new PrometheusRoute());
        getRoutes.put("/loggers", new LoggersRoute(adminConfig));
        getRoutes.put("/startup", new AdminProxyRoute(adminConfig, "/startup"));
        getRoutes.put("/classloaders", new AdminProxyRoute(adminConfig, "/classloaders"));
        getRoutes.put("/dependencies", new AdminProxyRoute(adminConfig, "/dependencies"));
        getRoutes.put("/requests", new AdminProxyRoute(adminConfig, "/requests"));
        postRoutes.put("/server/reload", new ServerRestartRoute(adminConfig));
        // LoggersRoute handles both GET and POST
        LoggersRoute loggersRoute = (LoggersRoute) getRoutes.get("/loggers");
        postRoutes.put("/loggers", loggersRoute);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String origin = req.getHeader("Origin");
        if (origin != null && origin.startsWith("http://localhost")) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        }
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/";

        try {
            if ("OPTIONS".equals(req.getMethod())) {
                String requestMethod = req.getHeader("Access-Control-Request-Method");
                if (requestMethod != null) {
                    resp.setHeader("Access-Control-Allow-Methods", requestMethod);
                }
                resp.getWriter().write("OK");
                return;
            }

            Route route = null;
            if ("GET".equals(req.getMethod())) {
                route = getRoutes.get(pathInfo);
            } else if ("POST".equals(req.getMethod())) {
                route = postRoutes.get(pathInfo);
            }

            if (route != null) {
                resp.setContentType("application/json");
                Object result = route.handle(req, resp);
                if (result != null) {
                    resp.getWriter().write(result.toString());
                }
            } else if ("GET".equals(req.getMethod())) {
                serveStaticFile(pathInfo, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void serveStaticFile(String pathInfo, HttpServletResponse resp) throws IOException {
        String resourcePath = "/public" + ("/".equals(pathInfo) ? "/index.html" : pathInfo);
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String mimeType = getServletContext().getMimeType(resourcePath);
        if (mimeType != null) {
            resp.setContentType(mimeType);
        }
        try (InputStream in = resource.openStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                resp.getOutputStream().write(buf, 0, len);
            }
        }
    }
}
