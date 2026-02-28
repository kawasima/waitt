package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import net.unit8.waitt.feature.dashboard.AdminConfig;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.InputStream;
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
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        if (!adminConfig.isAdminAvailable()) {
            response.status(403);
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
            try (InputStream is = conn.getInputStream()) {
                while (is.read() >= 0) { /* drain */ }
            }

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("detail", "Restarted successfully");
            return new Gson().toJson(body);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Server restart failed", e);
            response.status(500);
            Map<String, Object> problem = new HashMap<String, Object>();
            problem.put("title", "Internal Server Error");
            return new Gson().toJson(problem);
        } finally {
            conn.disconnect();
        }
    }
}
