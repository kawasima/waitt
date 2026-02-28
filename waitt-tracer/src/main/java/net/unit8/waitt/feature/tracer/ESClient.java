package net.unit8.waitt.feature.tracer;

import com.google.gson.*;
import net.unit8.waitt.feature.tracer.util.ISO8601Formatter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Elasticsearch REST client
 *
 * @author kawasima
 */
public class ESClient {
    private static final Logger LOG = Logger.getLogger(ESClient.class.getName());
    private final String baseUrl;

    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                    return src == null ? null : new JsonPrimitive(ISO8601Formatter.format(src));
                }
            })
            .create();

    public ESClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void post(String path, Serializable entry) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.connect();
            try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())) {
                out.write(gson.toJson(entry));
            }

            // Consume the response
            try (InputStream is = conn.getInputStream()) {
                while (is.read() != -1) { /* drain */ }
            }
        } catch(IOException e) {
            LOG.log(Level.WARNING, "Fail to post elasticsearch", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
