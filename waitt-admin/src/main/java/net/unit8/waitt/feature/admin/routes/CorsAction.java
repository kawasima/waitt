package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
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
        // Reuse the same loopback allow-list the actual GET/POST routes use, so
        // a preflight from 127.0.0.1 / [::1] doesn't fail where the real request
        // would succeed.
        ResponseUtils.applyCorsOrigin(exchange);
        exchange.getResponseHeaders().put("Access-Control-Allow-Methods",
                Arrays.asList("GET", "POST", "OPTIONS"));
        exchange.sendResponseHeaders(200, -1);
    }
}
