package net.unit8.waitt.feature.tracer;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet filter that creates OpenTelemetry spans for each HTTP request.
 *
 * <p>Uses reflection to bridge ClassLoader boundaries: the OTel Tracer is
 * created in the feature ClassRealm while this filter runs in the webapp
 * ClassLoader. All reflection is done once in {@link #init} and cached.</p>
 *
 * @author kawasima
 */
public class OtelTracingFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(OtelTracingFilter.class.getName());

    private Object tracer;
    private OtelReflection otel;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        tracer = System.getProperties().get("waitt.otel.tracer");
        if (tracer == null) {
            LOG.info("OpenTelemetry tracer not available. Tracing disabled.");
            return;
        }
        try {
            otel = new OtelReflection(tracer.getClass().getClassLoader());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to initialize OTel reflection. Tracing disabled.", e);
            tracer = null;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (tracer == null) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String method = req.getMethod();
        String path = req.getRequestURI();

        Object span = null;
        Object scope = null;
        try {
            Object builder = otel.spanBuilder.invoke(tracer, "HTTP " + method);
            builder = otel.setAttributeString.invoke(builder, "http.request.method", method);
            builder = otel.setAttributeString.invoke(builder, "url.path", path);
            builder = otel.setAttributeLong.invoke(builder, "server.port", (long) req.getServerPort());
            span = otel.startSpan.invoke(builder);

            scope = otel.makeCurrent.invoke(span);
            chain.doFilter(request, response);

            int statusCode = res.getStatus();
            otel.spanSetAttributeLong.invoke(span, "http.response.status_code", (long) statusCode);
            if (statusCode >= 500) {
                otel.setStatus.invoke(span, otel.statusError);
            }
        } catch (IOException | ServletException e) {
            recordError(span, e);
            throw e;
        } catch (RuntimeException e) {
            recordError(span, e);
            throw e;
        } catch (Exception e) {
            recordError(span, e);
            throw new ServletException(e);
        } finally {
            closeQuietly(scope);
            endQuietly(span);
        }
    }

    private void recordError(Object span, Exception e) {
        if (span == null || otel == null) return;
        try {
            String desc = e.getMessage() != null ? e.getMessage() : e.toString();
            otel.setStatusWithDesc.invoke(span, otel.statusError, desc);
            otel.recordException.invoke(span, e);
        } catch (Exception ignored) {}
    }

    private void closeQuietly(Object scope) {
        if (scope == null || otel == null) return;
        try { otel.scopeClose.invoke(scope); } catch (Exception ignored) {}
    }

    private void endQuietly(Object span) {
        if (span == null || otel == null) return;
        try { otel.spanEnd.invoke(span); } catch (Exception ignored) {}
    }

    @Override
    public void destroy() {
    }

    /**
     * Holds cached reflection references to OTel API methods.
     * Initialized once in {@link OtelTracingFilter#init}.
     */
    private static class OtelReflection {
        final Method spanBuilder;
        final Method setAttributeString;
        final Method setAttributeLong;
        final Method startSpan;
        final Method makeCurrent;
        final Method spanEnd;
        final Method spanSetAttributeLong;
        final Method setStatus;
        final Method setStatusWithDesc;
        final Method recordException;
        final Method scopeClose;
        final Object statusError;

        OtelReflection(ClassLoader cl) throws Exception {
            Class<?> tracerClass = cl.loadClass("io.opentelemetry.api.trace.Tracer");
            Class<?> spanBuilderClass = cl.loadClass("io.opentelemetry.api.trace.SpanBuilder");
            Class<?> spanClass = cl.loadClass("io.opentelemetry.api.trace.Span");
            Class<?> statusCodeClass = cl.loadClass("io.opentelemetry.api.trace.StatusCode");
            Class<?> scopeClass = cl.loadClass("io.opentelemetry.context.Scope");

            spanBuilder = tracerClass.getMethod("spanBuilder", String.class);
            setAttributeString = spanBuilderClass.getMethod("setAttribute", String.class, String.class);
            setAttributeLong = spanBuilderClass.getMethod("setAttribute", String.class, long.class);
            startSpan = spanBuilderClass.getMethod("startSpan");
            makeCurrent = spanClass.getMethod("makeCurrent");
            spanEnd = spanClass.getMethod("end");
            spanSetAttributeLong = spanClass.getMethod("setAttribute", String.class, long.class);
            setStatus = spanClass.getMethod("setStatus", statusCodeClass);
            setStatusWithDesc = spanClass.getMethod("setStatus", statusCodeClass, String.class);
            recordException = spanClass.getMethod("recordException", Throwable.class);
            scopeClose = scopeClass.getMethod("close");
            statusError = statusCodeClass.getField("ERROR").get(null);
        }
    }
}
