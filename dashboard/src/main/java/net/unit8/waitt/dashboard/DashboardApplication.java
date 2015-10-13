package net.unit8.waitt.dashboard;

import spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import spark.servlet.SparkApplication;

/**
 * @author kawasima
 */
public class DashboardApplication implements SparkApplication {
    @Override
    public void init() {
        get("/", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request request, Response response) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                return new ModelAndView(attributes, "index");
            }
        }, new ThymeleafTemplateEngine());
    }
}
