package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import net.unit8.waitt.feature.admin.routes.RequestLogAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class AdminApplication implements HttpHandler {
    private static final Logger LOG = Logger.getLogger(AdminApplication.class.getName());
    private final List<Route> routes = new ArrayList<Route>();

    public void addRoutes(Route... routes) {
        this.routes.addAll(Arrays.asList(routes));
    }
    @Override
    public void handle(HttpExchange exchange) {
        long startTime = System.nanoTime();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        int status = 200;
        try {
            for (Route route : routes) {
                if (route.canHandle(exchange)) {
                    route.handle(exchange);
                    return;
                }
            }
            status = 404;
            byte[] body404 = "Not found".getBytes("UTF-8");
            exchange.sendResponseHeaders(404, body404.length);
            exchange.getResponseBody().write(body404);
        } catch (Exception e) {
            status = 500;
            LOG.log(Level.SEVERE, "Error handling request", e);
            try {
                byte[] errorBody = "Internal Server Error".getBytes("UTF-8");
                exchange.sendResponseHeaders(500, errorBody.length);
                exchange.getResponseBody().write(errorBody);
            } catch (IOException ignored) {
                // Response may have already been sent
            }
        } finally {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            RequestLogAction.record(method, path, status, duration);
            exchange.close();
        }
    }
}
