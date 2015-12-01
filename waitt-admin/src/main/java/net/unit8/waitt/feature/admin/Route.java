package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * The interface of routing.
 *
 * @author kawasima
 */
public interface Route {
    /**
     * @param exchange http exchange
     * @return whether this route can handle the request.
     */
    boolean canHandle(HttpExchange exchange);

    /**
     * @param exchange http exchange
     * @throws IOException
     */
    void handle(HttpExchange exchange) throws IOException;
}
