package net.unit8.waitt.feature.dashboard;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Minimal route handler interface replacing spark.Route.
 */
@FunctionalInterface
public interface Route {
    Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
