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

public class ServerRoute implements Route {
    private AdminConfig adminConfig;

    public ServerRoute(AdminConfig adminConfig) {
        this.adminConfig = adminConfig;
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        if (adminConfig.isAdminAvailable()) {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + adminConfig.getAdminPort() + "/server").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (InputStream in = conn.getInputStream()) {
                Map<String, Object> serverData = new Gson().fromJson(
                        new InputStreamReader(in),
                        new TypeToken<Map<String, Object>>(){}.getType());
                if (serverData != null) {
                    attributes.putAll(serverData);
                }
            } catch (Exception e) {
                attributes.put("error", e.getMessage());
            } finally {
                conn.disconnect();
            }
        }
        attributes.put("adminAvailable", adminConfig.isAdminAvailable());
        attributes.put("context", request.getContextPath());
        return new Gson().toJson(attributes);
    }
}
