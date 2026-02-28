package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import net.unit8.waitt.api.dto.ServerMetadata;
import net.unit8.waitt.feature.dashboard.AdminConfig;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ServerRoute implements Route {
    private AdminConfig adminConfig;

    public ServerRoute(AdminConfig adminConfig) {
        this.adminConfig = adminConfig;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        if (adminConfig.isAdminAvailable()) {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + adminConfig.getAdminPort() + "/server").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (InputStream in = conn.getInputStream()) {
                ServerMetadata metadata = JAXB.unmarshal(in, ServerMetadata.class);
                attributes.put("serverMetadata", metadata);
            } finally {
                conn.disconnect();
            }
        }
        attributes.put("adminAvailable", adminConfig.isAdminAvailable());
        attributes.put("context", request.contextPath());
        return new Gson().toJson(attributes);
    }
}
