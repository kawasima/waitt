package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.unit8.waitt.feature.dashboard.AdminConfig;
import net.unit8.waitt.feature.dashboard.Route;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRestartRoute implements Route {
    private static final Logger LOG = Logger.getLogger(ServerRestartRoute.class.getName());
    private AdminConfig adminConfig;

    public ServerRestartRoute(AdminConfig adminConfig) {
        this.adminConfig = adminConfig;
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        if (!adminConfig.isAdminAvailable()) {
            response.setStatus(403);
            Map<String, Object> problem = new HashMap<String, Object>();
            problem.put("detail", "Admin feature is unavailable");
            return new Gson().toJson(problem);
        }
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + adminConfig.getAdminPort() + "/reload").openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        try {
            conn.getOutputStream().close();
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                Map<String, Object> body = new HashMap<String, Object>();
                body.put("detail", "Restarted successfully");
                return new Gson().toJson(body);
            } else {
                LOG.log(Level.SEVERE, "Server restart failed with HTTP " + code);
                response.setStatus(500);
                Map<String, Object> problem = new HashMap<String, Object>();
                problem.put("title", "Internal Server Error");
                return new Gson().toJson(problem);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Server restart failed", e);
            response.setStatus(500);
            Map<String, Object> problem = new HashMap<String, Object>();
            problem.put("title", "Internal Server Error");
            return new Gson().toJson(problem);
        } finally {
            conn.disconnect();
        }
    }
}
