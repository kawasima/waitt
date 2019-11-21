package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.*;

public class EnvPropertyRoute implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        List<KeyValuePair> environments = new ArrayList<KeyValuePair>();
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            environments.add(new KeyValuePair(e.getKey(), e.getValue()));
        }
        attributes.put("environments", environments);

        List<KeyValuePair> properties = new ArrayList<KeyValuePair>();
        Properties systemProperties = System.getProperties();
        for (String name : systemProperties.stringPropertyNames()) {
            properties.add(new KeyValuePair(name, systemProperties.getProperty(name)));
        }
        attributes.put("properties", properties);
        return new Gson().toJson(attributes);
    }

    private static class KeyValuePair {
        private final String key;
        private final String value;

        KeyValuePair(String key, String value){
            this.key = key;
            this.value =value;
        }
        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
    }
}
