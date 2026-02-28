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
        exchange.getResponseHeaders().put("Access-Control-Allow-Headers",
                Arrays.asList("Content-Type", "Accept"));
        exchange.getResponseHeaders().put("Access-Control-Allow-Methods",
                Arrays.asList("GET", "POST", "OPTIONS"));

        exchange.sendResponseHeaders(200, -1);
    }
}
