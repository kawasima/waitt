package net.unit8.waitt.feature.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter that creates OpenTelemetry spans for each HTTP request.
 *
 * @author kawasima
 */
public class ResponseDumpFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        Tracer tracer = TracerLifecycle.getTracer();
        if (tracer == null) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String method = req.getMethod();
        String path = req.getRequestURI();

        Span span = tracer.spanBuilder("HTTP " + method)
                .setAttribute("http.request.method", method)
                .setAttribute("url.path", path)
                .setAttribute("server.port", (long) req.getServerPort())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            chain.doFilter(request, response);
            int statusCode = res.getStatus();
            span.setAttribute("http.response.status_code", (long) statusCode);
            if (statusCode >= 500) {
                span.setStatus(StatusCode.ERROR);
            }
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public void destroy() {
    }
}
