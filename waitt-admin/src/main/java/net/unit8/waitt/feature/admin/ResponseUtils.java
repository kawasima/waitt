package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

public class ResponseUtils {
    public static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        byte[] body = message.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /**
     * True only when the Origin's host is exactly a loopback host. A prefix
     * check like {@code startsWith("http://localhost")} would also accept a
     * hostile origin such as {@code http://localhost.attacker.example}.
     */
    static boolean isLocalOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        try {
            String host = new URI(origin).getHost();
            return "localhost".equals(host)
                    || "127.0.0.1".equals(host)
                    || "::1".equals(host)
                    || "[::1]".equals(host);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Echo the request Origin into the CORS allow-origin header only when it is
     * a loopback origin. Shared by every admin route that answers cross-origin
     * fetches so the allow-list lives in one place.
     */
    public static void applyCorsOrigin(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (isLocalOrigin(origin)) {
            exchange.getResponseHeaders().put("Access-Control-Allow-Origin", Collections.singletonList(origin));
        }
        exchange.getResponseHeaders().put("Access-Control-Allow-Headers", Collections.singletonList("Content-Type, Accept"));
    }

    public static void responseJSON(HttpExchange exchange, JSONObject jsonObject) throws IOException {
        applyCorsOrigin(exchange);
        byte[] json = jsonObject.toJSONString().getBytes("UTF-8");
        exchange.sendResponseHeaders(200, json.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json);
        }
    }
}
