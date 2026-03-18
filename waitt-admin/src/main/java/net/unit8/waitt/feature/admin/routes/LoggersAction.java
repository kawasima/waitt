package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List loggers and change log levels dynamically.
 * Supports SLF4J Logback via reflection, with j.u.l as fallback.
 *
 * @author kawasima
 */
public class LoggersAction implements Route {
    private static final Logger LOG = Logger.getLogger(LoggersAction.class.getName());

    @Override
    public boolean canHandle(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        return (path.equals("/loggers") || path.startsWith("/loggers/")) &&
                ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePost(exchange);
        } else {
            handleGet(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        List<JSONObject> loggers = new ArrayList<JSONObject>();

        // Try SLF4J Logback first
        if (!tryLogbackLoggers(loggers)) {
            // Fallback to j.u.l
            julLoggers(loggers);
        }

        json.put("loggers", loggers);
        json.put("backend", detectBackend());
        ResponseUtils.responseJSON(exchange, json);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        String bodyStr = body.toString();

        // Read name from path (/loggers/{name}) or from body ({"name":"...","level":"..."})
        String path = exchange.getRequestURI().getPath();
        String loggerName;
        if (path.length() > "/loggers/".length()) {
            loggerName = path.substring("/loggers/".length());
        } else {
            loggerName = extractField(bodyStr, "name");
        }
        String level = extractField(bodyStr, "level");

        boolean success = false;
        if (loggerName != null && level != null) {
            success = trySetLogbackLevel(loggerName, level) || setJulLevel(loggerName, level);
        }

        JSONObject json = new JSONObject();
        json.put("logger", loggerName);
        json.put("level", level);
        json.put("success", success);
        ResponseUtils.responseJSON(exchange, json);
    }

    private String extractField(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    private String detectBackend() {
        try {
            Class.forName("ch.qos.logback.classic.Logger");
            return "logback";
        } catch (ClassNotFoundException e) {
            // not logback
        }
        return "jul";
    }

    private boolean tryLogbackLoggers(List<JSONObject> loggers) {
        try {
            Class<?> loggerFactoryClass = Class.forName("org.slf4j.LoggerFactory");
            Method getILoggerFactory = loggerFactoryClass.getMethod("getILoggerFactory");
            Object factory = getILoggerFactory.invoke(null);

            Class<?> loggerContextClass = Class.forName("ch.qos.logback.classic.LoggerContext");
            if (!loggerContextClass.isInstance(factory)) return false;

            Method getLoggerList = loggerContextClass.getMethod("getLoggerList");
            @SuppressWarnings("unchecked")
            List<?> logbackLoggers = (List<?>) getLoggerList.invoke(factory);

            Class<?> logbackLoggerClass = Class.forName("ch.qos.logback.classic.Logger");
            Method getName = logbackLoggerClass.getMethod("getName");
            Method getLevel = logbackLoggerClass.getMethod("getLevel");
            Method getEffectiveLevel = logbackLoggerClass.getMethod("getEffectiveLevel");

            for (Object logger : logbackLoggers) {
                JSONObject entry = new JSONObject();
                entry.put("name", (String) getName.invoke(logger));
                Object level = getLevel.invoke(logger);
                entry.put("configuredLevel", level != null ? level.toString() : null);
                entry.put("effectiveLevel", getEffectiveLevel.invoke(logger).toString());
                loggers.add(entry);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean trySetLogbackLevel(String loggerName, String level) {
        try {
            Class<?> loggerFactoryClass = Class.forName("org.slf4j.LoggerFactory");
            Method getLogger = loggerFactoryClass.getMethod("getLogger", String.class);
            Object logger = getLogger.invoke(null, loggerName);

            Class<?> logbackLoggerClass = Class.forName("ch.qos.logback.classic.Logger");
            if (!logbackLoggerClass.isInstance(logger)) return false;

            Class<?> logbackLevelClass = Class.forName("ch.qos.logback.classic.Level");
            Method toLevel = logbackLevelClass.getMethod("toLevel", String.class);
            Object levelObj = toLevel.invoke(null, level);

            Method setLevel = logbackLoggerClass.getMethod("setLevel", logbackLevelClass);
            setLevel.invoke(logger, levelObj);
            LOG.info("Set logger " + loggerName + " to " + level + " (logback)");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void julLoggers(List<JSONObject> loggers) {
        java.util.logging.LogManager manager = java.util.logging.LogManager.getLogManager();
        Enumeration<String> names = manager.getLoggerNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Logger logger = manager.getLogger(name);
            if (logger != null) {
                JSONObject entry = new JSONObject();
                entry.put("name", name.isEmpty() ? "ROOT" : name);
                Level lv = logger.getLevel();
                entry.put("configuredLevel", lv != null ? lv.getName() : null);
                entry.put("effectiveLevel", logger.getParent() != null && lv == null
                        ? logger.getParent().getLevel() != null ? logger.getParent().getLevel().getName() : "INFO"
                        : lv != null ? lv.getName() : "INFO");
                loggers.add(entry);
            }
        }
    }

    private boolean setJulLevel(String loggerName, String level) {
        try {
            String julName = "ROOT".equals(loggerName) ? "" : loggerName;
            Logger logger = Logger.getLogger(julName);
            logger.setLevel(Level.parse(level.toUpperCase()));
            LOG.info("Set logger " + loggerName + " to " + level + " (jul)");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
