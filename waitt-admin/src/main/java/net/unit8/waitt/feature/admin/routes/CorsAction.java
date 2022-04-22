package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.Route;

import java.io.IOException;
import java.util.Arrays;

public class CorsAction implements Route {
    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "OPTIONS".equalsIgnoreCase(exchange.getRequestMethod());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String accessControlRequestHeaders = exchange.getRequestHeaders().getFirst("Access-Control-Request-Headers");
        if (accessControlRequestHeaders != null) {
            exchange.getResponseHeaders().put("Access-Control-Allow-Headers",
                    Arrays.asList(accessControlRequestHeaders));
        }

        String accessControlRequestMethods = exchange.getRequestHeaders().getFirst("Access-Control-Request-Methods");
        if (accessControlRequestMethods != null) {
            exchange.getResponseHeaders().put("Access-Control-Allow-Methods",
                    Arrays.asList(accessControlRequestMethods));
        }

        exchange.sendResponseHeaders(200, -1);
    }
}
