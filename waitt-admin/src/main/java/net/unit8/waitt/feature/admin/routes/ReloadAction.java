package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.feature.admin.Route;

import java.io.IOException;

/**
 * Provide a feature to reload the server.
 *
 * @author kawasima
 */
public class ReloadAction implements Route {
    final EmbeddedServer server;

    public ReloadAction(EmbeddedServer server) {
        this.server = server;
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "POST".equalsIgnoreCase(exchange.getRequestMethod())
                && "/reload".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        server.reload();
        exchange.sendResponseHeaders(204, 0);
    }
}
