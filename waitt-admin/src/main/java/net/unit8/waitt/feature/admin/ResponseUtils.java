package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.util.Collections;

public class ResponseUtils {
    public static void responseJSON(HttpExchange exchange, JSONObject jsonObject) throws IOException {
        exchange.getResponseHeaders().put("content-type", Collections.singletonList("application/json"));
        exchange.getResponseHeaders().put("Access-Control-Allow-Origin", Collections.singletonList("*"));
        exchange.getResponseHeaders().put("Access-Control-Allow-Headers", Collections.singletonList("*"));

        byte[] json = jsonObject.toJSONString().getBytes("UTF-8");
        exchange.sendResponseHeaders(200, json.length);
        exchange.getResponseBody().write(json);
    }
}
