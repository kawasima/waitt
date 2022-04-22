package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONArray;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class EnvPropertyAction implements Route {
   @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/env".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        JSONArray environments = new JSONArray();
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            JSONObject env = new JSONObject();
            env.put("key", e.getKey());
            env.put("value", e.getValue());
            environments.add(env);
        }
        json.put("environments", environments);

        JSONArray properties = new JSONArray();
        Properties systemProperties = System.getProperties();
        for (String name : systemProperties.stringPropertyNames()) {
            JSONObject property = new JSONObject();
            property.put("key", name);
            property.put("value", systemProperties.getProperty(name));
            properties.add(property);
        }
        json.put("properties", properties);
        ResponseUtils.responseJSON(exchange, json);
    }
}
