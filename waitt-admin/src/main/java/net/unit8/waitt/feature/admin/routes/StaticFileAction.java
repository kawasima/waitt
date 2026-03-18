package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.Route;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Collections;

/**
 * Serves static files (dashboard UI) from classpath resources.
 * This route should be added last as a fallback.
 *
 * @author kawasima
 */
public class StaticFileAction implements Route {
    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        // Decode percent-encoded sequences before checking for traversal
        String decoded = URLDecoder.decode(path, "UTF-8");
        if (decoded.contains("..") || decoded.contains("\\")) {
            byte[] body = "Forbidden".getBytes("UTF-8");
            exchange.sendResponseHeaders(403, body.length);
            exchange.getResponseBody().write(body);
            return;
        }
        path = decoded;
        if ("/".equals(path) || path.isEmpty()) {
            path = "/index.html";
        }
        String resourcePath = "/public" + path;
        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) {
            byte[] body = "Not found".getBytes("UTF-8");
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            return;
        }

        String contentType = guessContentType(path);
        exchange.getResponseHeaders().put("Content-Type", Collections.singletonList(contentType));
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream os = exchange.getResponseBody()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
        } finally {
            in.close();
        }
    }

    private String guessContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
