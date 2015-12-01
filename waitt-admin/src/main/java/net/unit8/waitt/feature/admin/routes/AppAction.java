package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.feature.admin.Route;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Provide information about the application.
 *
 * @author kawasima
 */
public class AppAction implements Route {
    final WebappConfiguration config;

    public AppAction(WebappConfiguration config) {
        this.config = config;
    }
    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/app".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        StringWriter sw = new StringWriter();
        JAXB.marshal(config, sw);
        byte[] body = sw.toString().getBytes("UTF-8");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
    }
}
