package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.feature.dashboard.AdminConfig;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ApplicationRoute implements Route {
    private final AdminConfig adminConfig;

    public ApplicationRoute(AdminConfig adminConfig) {
        this.adminConfig = adminConfig;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        if (adminConfig.isAdminAvailable()) {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + adminConfig.getAdminPort() + "/app").openConnection();
            InputStream in = conn.getInputStream();
            try {
                WebappConfiguration config = JAXB.unmarshal(in, WebappConfiguration.class);
                attributes.put("configuration", config);
            } finally {
                in.close();
                conn.disconnect();
            }
        }
        attributes.put("adminAvailable", adminConfig.isAdminAvailable());

        response.type("application/json");
        return new Gson().toJson(attributes);
    }
}
