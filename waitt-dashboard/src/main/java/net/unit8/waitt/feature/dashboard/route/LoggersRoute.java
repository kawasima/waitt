package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.unit8.waitt.feature.dashboard.AdminConfig;
import net.unit8.waitt.feature.dashboard.Route;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class LoggersRoute implements Route {
    private final AdminConfig adminConfig;

    public LoggersRoute(AdminConfig adminConfig) {
        this.adminConfig = adminConfig;
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!adminConfig.isAdminAvailable()) {
            return new Gson().toJson(Collections.singletonMap("error", "Admin not available"));
        }

        if ("POST".equals(request.getMethod())) {
            return handlePost(request);
        }
        return handleGet();
    }

    private Object handleGet() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(
                "http://localhost:" + adminConfig.getAdminPort() + "/loggers").openConnection();
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

    private Object handlePost(HttpServletRequest request) throws Exception {
        // Read body: {"name":"com.example.Foo","level":"DEBUG"}
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        String bodyStr = body.toString();

        // Extract name from body to build admin URL
        Gson gson = new Gson();
        Map<String, String> bodyMap = gson.fromJson(bodyStr, new TypeToken<Map<String, String>>(){}.getType());
        String loggerName = bodyMap != null ? bodyMap.get("name") : "";

        String adminUrl = "http://localhost:" + adminConfig.getAdminPort() + "/loggers/" + loggerName;
        HttpURLConnection conn = (HttpURLConnection) new URL(adminUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyStr.getBytes("UTF-8"));
        }
        try (InputStream in = conn.getInputStream()) {
            Map<String, Object> result = gson.fromJson(
                    new InputStreamReader(in), new TypeToken<Map<String, Object>>(){}.getType());
            return gson.toJson(result);
        } finally {
            conn.disconnect();
        }
    }
}
