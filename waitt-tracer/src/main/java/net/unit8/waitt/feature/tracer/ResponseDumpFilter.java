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
 * Uses reflection to access the Tracer across ClassLoader boundaries.
 *
 * @author kawasima
 */
public class ResponseDumpFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(ResponseDumpFilter.class.getName());

    private Object tracer;
    private Method spanBuilderMethod;
    private Method setAttributeStringMethod;
    private Method setAttributeLongMethod;
    private Method startSpanMethod;
    private Method makeCurrentMethod;
    private Method endMethod;
    private Method setStatusMethod;
    private Method setStatusWithDescMethod;
    private Method recordExceptionMethod;
    private Method scopeCloseMethod;
    private Object statusError;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        tracer = System.getProperties().get("waitt.otel.tracer");
        if (tracer == null) {
            LOG.info("OpenTelemetry tracer not found. Tracing disabled.");
            return;
        }
        try {
            ClassLoader otelClassLoader = tracer.getClass().getClassLoader();
            Class<?> tracerClass = otelClassLoader.loadClass("io.opentelemetry.api.trace.Tracer");
            Class<?> spanBuilderClass = otelClassLoader.loadClass("io.opentelemetry.api.trace.SpanBuilder");
            Class<?> spanClass = otelClassLoader.loadClass("io.opentelemetry.api.trace.Span");
            Class<?> statusCodeClass = otelClassLoader.loadClass("io.opentelemetry.api.trace.StatusCode");
            Class<?> scopeClass = otelClassLoader.loadClass("io.opentelemetry.context.Scope");
            Class<?> attributeKeyClass = otelClassLoader.loadClass("io.opentelemetry.api.common.AttributeKey");

            spanBuilderMethod = tracerClass.getMethod("spanBuilder", String.class);
            spanBuilderMethod.setAccessible(true);

            // SpanBuilder methods
            Method sbSetAttributeString = spanBuilderClass.getMethod("setAttribute", String.class, String.class);
            sbSetAttributeString.setAccessible(true);
            Method sbSetAttributeKey = spanBuilderClass.getMethod("setAttribute", attributeKeyClass, Object.class);
            sbSetAttributeKey.setAccessible(true);
            startSpanMethod = spanBuilderClass.getMethod("startSpan");
            startSpanMethod.setAccessible(true);

            // Use setAttribute(String, String) for string attributes
            setAttributeStringMethod = sbSetAttributeString;

            // For long attributes, use AttributeKey.longKey()
            Method longKeyMethod = attributeKeyClass.getMethod("longKey", String.class);
            setAttributeLongMethod = sbSetAttributeKey;

            // Span methods
            makeCurrentMethod = spanClass.getMethod("makeCurrent");
            makeCurrentMethod.setAccessible(true);
            endMethod = spanClass.getMethod("end");
            endMethod.setAccessible(true);
            setStatusMethod = spanClass.getMethod("setStatus", statusCodeClass);
            setStatusMethod.setAccessible(true);
            setStatusWithDescMethod = spanClass.getMethod("setStatus", statusCodeClass, String.class);
            setStatusWithDescMethod.setAccessible(true);
            recordExceptionMethod = spanClass.getMethod("recordException", Throwable.class);
            recordExceptionMethod.setAccessible(true);

            // Scope close
            scopeCloseMethod = scopeClass.getMethod("close");
            scopeCloseMethod.setAccessible(true);

            // StatusCode.ERROR
            statusError = statusCodeClass.getField("ERROR").get(null);

            // Cache longKey method for attribute setting
            System.getProperties().put("waitt.otel.longKeyMethod", longKeyMethod);

            LOG.info("OpenTelemetry tracing filter initialized.");
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
            Method longKeyMethod = (Method) System.getProperties().get("waitt.otel.longKeyMethod");

            // Build span
            Object builder = spanBuilderMethod.invoke(tracer, "HTTP " + method);
            builder = setAttributeStringMethod.invoke(builder, "http.request.method", method);
            builder = setAttributeStringMethod.invoke(builder, "url.path", path);
            Object portKey = longKeyMethod.invoke(null, "server.port");
            builder = setAttributeLongMethod.invoke(builder, portKey, (long) req.getServerPort());
            span = startSpanMethod.invoke(builder);

            scope = makeCurrentMethod.invoke(span);
            chain.doFilter(request, response);

            int statusCode = res.getStatus();
            Object statusCodeKey = longKeyMethod.invoke(null, "http.response.status_code");
            Method spanSetAttribute = span.getClass().getMethod("setAttribute",
                    span.getClass().getClassLoader().loadClass("io.opentelemetry.api.common.AttributeKey"),
                    Object.class);
            spanSetAttribute.setAccessible(true);
            spanSetAttribute.invoke(span, statusCodeKey, (long) statusCode);
            if (statusCode >= 500) {
                setStatusMethod.invoke(span, statusError);
            }
        } catch (IOException | ServletException e) {
            if (span != null) {
                try {
                    setStatusWithDescMethod.invoke(span, statusError, e.getMessage());
                    recordExceptionMethod.invoke(span, e);
                } catch (Exception ignored) {}
            }
            throw e;
        } catch (Exception e) {
            if (span != null) {
                try {
                    setStatusWithDescMethod.invoke(span, statusError, e.getMessage());
                    recordExceptionMethod.invoke(span, e);
                } catch (Exception ignored) {}
            }
            throw new ServletException(e);
        } finally {
            if (scope != null) {
                try { scopeCloseMethod.invoke(scope); } catch (Exception ignored) {}
            }
            if (span != null) {
                try { endMethod.invoke(span); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void destroy() {
    }
}
