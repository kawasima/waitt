package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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
        try {
            for (Route route : routes) {
                if (route.canHandle(exchange)) {
                    route.handle(exchange);
                    return;
                }
            }
            ResponseUtils.sendError(exchange, 404, "Not found");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error handling request", e);
            try {
                ResponseUtils.sendError(exchange, 500, "Internal Server Error");
            } catch (IOException ignored) {
                // Response may have already been sent
            }
        } finally {
            exchange.close();
        }
    }
}
