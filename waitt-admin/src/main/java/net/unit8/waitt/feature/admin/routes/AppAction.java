package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONArray;
import net.unit8.waitt.feature.admin.json.JSONObject;

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
                && "/application".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        StringWriter sw = new StringWriter();
        JSONObject json = new JSONObject();
        json.put("applicationName", config.getApplicationName());
        json.put("sourceDirectory", config.getSourceDirectory().getAbsolutePath());
        json.put("baseDirectory", config.getBaseDirectory().getAbsolutePath());
        JSONArray features = new JSONArray();
        for (Feature feature : config.getFeatures()) {
            JSONObject featureJson = new JSONObject();
            featureJson.put("groupId", feature.getGroupId());
            featureJson.put("artifactId", feature.getArtifactId());
            featureJson.put("version", feature.getVersion());
            featureJson.put("type", feature.getType());
            if (feature.getConfiguration() != null) {
                featureJson.put("configuration", new JSONObject(feature.getConfiguration()));
            }
            features.add(featureJson);
        }
        json.put("features", features);

        json.put("packages", new JSONArray(config.getPackages()));

        ResponseUtils.responseJSON(exchange, json);
    }
}
