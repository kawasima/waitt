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
import java.util.Collections;
import java.util.Map;

/**
 * Generic proxy route that forwards GET requests to the admin server.
 */
public class AdminProxyRoute implements Route {
    private final AdminConfig adminConfig;
    private final String adminPath;

    public AdminProxyRoute(AdminConfig adminConfig, String adminPath) {
        this.adminConfig = adminConfig;
        this.adminPath = adminPath;
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!adminConfig.isAdminAvailable()) {
            return new Gson().toJson(Collections.singletonMap("error", "Admin not available"));
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(
                "http://localhost:" + adminConfig.getAdminPort() + adminPath).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        try (InputStream in = conn.getInputStream()) {
            Map<String, Object> data = new Gson().fromJson(
                    new InputStreamReader(in), new TypeToken<Map<String, Object>>(){}.getType());
            return new Gson().toJson(data);
        } catch (Exception e) {
            return new Gson().toJson(Collections.singletonMap("error", e.getMessage()));
        } finally {
            conn.disconnect();
        }
    }
}
