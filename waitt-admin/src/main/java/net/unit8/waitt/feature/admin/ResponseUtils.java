package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class ResponseUtils {
    public static void responseJSON(HttpExchange exchange, JSONObject jsonObject) throws IOException {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin != null && origin.startsWith("http://localhost")) {
            exchange.getResponseHeaders().put("Access-Control-Allow-Origin", Collections.singletonList(origin));
        }
        exchange.getResponseHeaders().put("Access-Control-Allow-Headers", Collections.singletonList("Content-Type, Accept"));
        byte[] json = jsonObject.toJSONString().getBytes("UTF-8");
        exchange.sendResponseHeaders(200, json.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json);
        }
    }
}
