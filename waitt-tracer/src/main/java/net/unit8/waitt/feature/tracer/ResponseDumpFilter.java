package net.unit8.waitt.feature.tracer;

import net.unit8.waitt.feature.tracer.entry.ResponseEntry;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
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
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return new TeeOutputStream(super.getOutputStream(), System.out);
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return new TeeWriter(super.getWriter(), writer);
            }
        };
        chain.doFilter(request, responseWrapper);
        ResponseEntry responseEntry = new ResponseEntry(((HttpServletRequest) request).getRequestURI(), writer.toString());
        esClient.post("/waitt/response/", responseEntry);

    }

    @Override
    public void destroy() {

    }
}
