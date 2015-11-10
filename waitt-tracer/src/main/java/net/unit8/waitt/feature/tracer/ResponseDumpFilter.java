package net.unit8.waitt.feature.tracer;

import net.unit8.waitt.feature.tracer.entry.ResponseEntry;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
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
        final AtomicInteger statusCode = new AtomicInteger(200);

        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response) {
            @Override
            public PrintWriter getWriter() throws IOException {
                return new TeeWriter(super.getWriter(), writer);
            }

            @Override
            public void setStatus(int sc) {
                super.setStatus(sc);
                statusCode.set(sc);
            }

            @Override
            public void setStatus(int sc, String reason) {
                super.setStatus(sc, reason);
                statusCode.set(sc);
            }
        };
        chain.doFilter(request, responseWrapper);
        String contentType = responseWrapper.getContentType();
        if (contentType != null && contentType.startsWith("text/html")) {
            ResponseEntry responseEntry = new ResponseEntry(
                    ((HttpServletRequest) request).getRequestURI(),
                    statusCode.get(),
                    writer.toString());
            esClient.post("/waitt/response/", responseEntry);
        }
    }

    @Override
    public void destroy() {

    }
}
