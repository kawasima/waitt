package net.unit8.waitt.feature.dashboard;

import com.sun.management.HotSpotDiagnosticMXBean;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.api.dto.ServerMetadata;
import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.gridkit.jvmtool.heapdump.StringCollector;
import org.netbeans.lib.profiler.heap.FastHprofHeap;
import org.netbeans.lib.profiler.heap.Heap;
import spark.*;
import spark.servlet.SparkApplication;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static spark.Spark.*;

/**
 * @author kawasima
 */
public class DashboardApplication implements SparkApplication {
    
    @Override
    public void init() {
        final AdminConfig adminConfig = new AdminConfig();
        adminConfig.read();

        TemplateEngine engine = new ThymeleafTemplateEngine();
        staticFileLocation("/public");
        get("/", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request request, Response response) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                if (adminConfig.isAdminAvailable()) {
                    HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + adminConfig.getAdminPort() + "/app").openConnection();
                    InputStream in = conn.getInputStream();
                    try {
                        WebappConfiguration config = JAXB.unmarshal(in, WebappConfiguration.class);
                        attributes.put("webappConfiguration", config);
                    } finally {
                        in.close();
                        conn.disconnect();
                    }
                }
                attributes.put("adminAvailable", adminConfig.isAdminAvailable());
                attributes.put("context", request.contextPath());
                return new ModelAndView(attributes, "application");
            }
        }, engine);

        get("/server", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request request, Response response) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                if (adminConfig.isAdminAvailable()) {
                    InputStream in = new URL("http://localhost:" + adminConfig.getAdminPort() + "/server").openStream();
                    try {
                        ServerMetadata metadata = JAXB.unmarshal(in, ServerMetadata.class);
                        attributes.put("serverMetadata", metadata);
                    } finally {
                        in.close();
                    }
                }
                attributes.put("adminAvailable", adminConfig.isAdminAvailable());
                attributes.put("context", request.contextPath());
                return new ModelAndView(attributes, "server");
            }
        }, engine);

        post("/server/reload", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                if (!adminConfig.isAdminAvailable()) {
                    response.status(403);
                    response.body("Admin feature is unavailable.");
                    return null;
                }
                HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + adminConfig.getAdminPort() + "/reload").openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                try {
                    conn.getOutputStream().close();
                    InputStream is = conn.getInputStream();
                    try {
                        //noinspection StatementWithEmptyBody
                        while (is.read() >= 0) ;
                        is.close();
                    } finally {
                        is.close();
                    }

                    response.redirect(request.contextPath()
                            + "/server");
                    return null;
                } catch (Exception e) {
                    response.status(500);
                    return null;
                } finally {
                    conn.disconnect();
                }
            }
        });

        get("/env", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request request, Response response) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("environments", System.getenv());
                attributes.put("context", request.contextPath());

                return new ModelAndView(attributes, "env");
            }
        }, engine);

        get("/property", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request request, Response response) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("properties", System.getProperties());
                attributes.put("context", request.contextPath());

                return new ModelAndView(attributes, "property");
            }
        }, engine);

        get("/heap", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request request, Response response) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("heapdump", heapDump());
                attributes.put("context", request.contextPath());

                return new ModelAndView(attributes, "heap");
            }
        }, engine);
        
        get("/thread", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request rqst, Response rspns) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("threaddump", threadDump());
                attributes.put("menu", "heap");
                
                return new ModelAndView(attributes, "thread");
            }
        }, engine);
        

    }
    
    private List<HeapHistogram.ClassRecord> heapDump() throws IOException {
        File heapDumpDirectory = new File("target");
        File dumpFile = null;
        try {
            dumpFile = File.createTempFile("waitt", ".hprof", heapDumpDirectory);
            if (dumpFile.exists()) {
                dumpFile.delete();
            }
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap(dumpFile.getPath(), true);
            Heap heap = new FastHprofHeap(dumpFile, 0);
            StringCollector collector = new StringCollector();
            HeapHistogram histo = new HeapHistogram();
            collector.collect(heap, histo);
            List<HeapHistogram.ClassRecord> ht = new ArrayList<HeapHistogram.ClassRecord>(histo.getHisto());
            ht.add(collector.asClassRecord());
            Collections.sort(ht, HeapHistogram.BY_SIZE);
            return ht.subList(0, 500);
        } finally {
            if (dumpFile != null && dumpFile.exists()) {
                dumpFile.delete();
            }
        }
    }
    
    private List<ThreadInfo> threadDump() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = mxBean.getThreadInfo(mxBean.getAllThreadIds(), 0);
        return Arrays.asList(threadInfos);
    }
}
