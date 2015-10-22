package net.unit8.waitt.feature.dashboard;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.gridkit.jvmtool.heapdump.StringCollector;
import org.netbeans.lib.profiler.heap.FastHprofHeap;
import org.netbeans.lib.profiler.heap.Heap;

import static spark.Spark.*;
import spark.servlet.SparkApplication;

/**
 * @author kawasima
 */
public class DashboardApplication implements SparkApplication {
    
    @Override
    public void init() {
        TemplateEngine engine = new ThymeleafTemplateEngine();
        get("/", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request request, Response response) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("menu", "overview");
                return new ModelAndView(attributes, "index");
            }
        }, engine);
        
        get("/env", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request rqst, Response rspns) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("environments", System.getenv());
                attributes.put("menu", "env");
                
                return new ModelAndView(attributes, "index");
            }
        }, engine);

        get("/heap", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request rqst, Response rspns) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("heapdump", heapDump());
                attributes.put("menu", "heap");
                
                return new ModelAndView(attributes, "index");
            }
        }, engine);
        
        get("/thread", new TemplateViewRoute() {
            @Override
            public ModelAndView handle(Request rqst, Response rspns) throws Exception {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("threaddump", threadDump());
                attributes.put("menu", "heap");
                
                return new ModelAndView(attributes, "index");
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
