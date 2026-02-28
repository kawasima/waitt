package net.unit8.waitt.feature.tracer;

import net.unit8.waitt.feature.tracer.entry.ResponseEntry;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class ResponseDumpFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(ResponseDumpFilter.class.getName());
    private ESClient esClient;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO config
        esClient = new ESClient("http://localhost:9200");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        LOG.info(((HttpServletRequest) request).getRequestURI());
        final StringWriter writer = new StringWriter();
        final ByteArrayOutputStream byteCapture = new ByteArrayOutputStream();
        final AtomicInteger statusCode = new AtomicInteger(200);

        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response) {
            @Override
            public PrintWriter getWriter() throws IOException {
                return new TeeWriter(super.getWriter(), writer);
            }

            @Override
            public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
                return new TeeOutputStream(super.getOutputStream(), byteCapture);
            }

            @Override
            public void setStatus(int sc) {
                super.setStatus(sc);
                statusCode.set(sc);
            }

            @Override
            public void sendError(int sc) throws IOException {
                super.sendError(sc);
                statusCode.set(sc);
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                statusCode.set(sc);
                super.sendError(sc, msg);
            }

            @Override
            public void sendRedirect(String location) throws IOException {
                statusCode.set(302);
                super.sendRedirect(location);
            }
        };
        chain.doFilter(request, responseWrapper);
        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.startsWith("text/html")) {
            String responseBody = writer.toString();
            if (responseBody.isEmpty() && byteCapture.size() > 0) {
                responseBody = byteCapture.toString("UTF-8");
            }
            ResponseEntry responseEntry = new ResponseEntry(
                    ((HttpServletRequest) request).getRequestURI(),
                    statusCode.get(),
                    responseBody);
            esClient.post("/waitt/response/", responseEntry);
        }
    }

    @Override
    public void destroy() {

    }
}
