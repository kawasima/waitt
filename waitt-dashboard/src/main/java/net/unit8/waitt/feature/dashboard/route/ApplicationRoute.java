package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.unit8.waitt.feature.dashboard.AdminConfig;
import net.unit8.waitt.feature.dashboard.Route;

import java.io.InputStream;
import java.io.InputStreamReader;
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
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        if (adminConfig.isAdminAvailable()) {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + adminConfig.getAdminPort() + "/application").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (InputStream in = conn.getInputStream()) {
                Map<String, Object> config = new Gson().fromJson(
                        new InputStreamReader(in),
                        new TypeToken<Map<String, Object>>(){}.getType());
                attributes.put("configuration", config);
            } catch (Exception e) {
                attributes.put("error", e.getMessage());
            } finally {
                conn.disconnect();
            }
        }
        attributes.put("adminAvailable", adminConfig.isAdminAvailable());

        response.setContentType("application/json");
        return new Gson().toJson(attributes);
    }
}
