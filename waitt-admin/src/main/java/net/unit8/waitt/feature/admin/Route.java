package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * @author kawasima
 */
public interface Route {
    boolean canHandle(HttpExchange exchange);
    void handle(HttpExchange exchange) throws IOException;
}
