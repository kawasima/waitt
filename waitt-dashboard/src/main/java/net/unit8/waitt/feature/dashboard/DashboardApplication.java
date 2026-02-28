package net.unit8.waitt.feature.dashboard;

import net.unit8.waitt.feature.dashboard.route.*;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.servlet.SparkApplication;

import static spark.Spark.*;

/**
 * @author kawasima
 */
public class DashboardApplication implements SparkApplication {
    @Override
    public void init() {
        final AdminConfig adminConfig = new AdminConfig();
        adminConfig.read();
        options("/*",
                new Route() {
                    @Override
                    public Object handle(Request request, Response response) throws Exception {
                        String origin = request.headers("Origin");
                        if (origin != null && origin.startsWith("http://localhost")) {
                            response.header("Access-Control-Allow-Origin", origin);
                        }
                        response.header("Access-Control-Allow-Headers", "Content-Type, Accept");

                        String accessControlRequestMethod = request
                                .headers("Access-Control-Request-Method");
                        if (accessControlRequestMethod != null) {
                            response.header("Access-Control-Allow-Methods",
                                    accessControlRequestMethod);
                        }

                        return "OK";
                    }
                });
        before(new Filter() {
            @Override
            public void handle(Request req, Response res) throws Exception {
                String origin = req.headers("Origin");
                if (origin != null && origin.startsWith("http://localhost")) {
                    res.header("Access-Control-Allow-Origin", origin);
                }
                res.header("Access-Control-Allow-Headers", "Content-Type, Accept");
                res.type("application/json");
            }
        });
        get("/application", new ApplicationRoute(adminConfig));
        get("/server", new ServerRoute(adminConfig));
        post("/server/reload", new ServerRestartRoute(adminConfig));
        get("/env", new EnvPropertyRoute());
        get("/heap", new HeapDumpRoute());
        get("/thread", new ThreadDumpRoute());
        get("/prometheus", new PrometheusRoute());
        staticFileLocation("/public");
    }
    

}
