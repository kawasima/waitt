package net.unit8.waitt;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class WaittServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(WaittServlet.class.getName());

    private final EmbeddedServer server;
    private final ExecutorService executorService;

    WaittServlet(EmbeddedServer server, ExecutorService executorService) {
        this.server = server;
        this.executorService = executorService;
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("SHUTDOWN") != null) {
            logger.info("Accept a tomcat stop request.");
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        server.stop();
                    } catch(Exception e) {
                        logger.log(Level.SEVERE, "Tomcat stop failure.", e);
                    }
                }
            });
            PrintWriter pw = response.getWriter();
            pw.println("Shutdown tomcat.");
        }
    }
}
