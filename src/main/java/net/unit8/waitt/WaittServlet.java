package net.unit8.waitt;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kawasima
 */
public class WaittServlet extends HttpServlet {
    private Server server;
    WaittServlet(Server server) {
        this.server = server;
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        if (request.getParameter("SHUTDOWN") != null) {
            try {
                server.stop();
            } catch(LifecycleException e) {
                throw new ServletException("Tomcat stop failure.", e);
            }
        }
    }
}
