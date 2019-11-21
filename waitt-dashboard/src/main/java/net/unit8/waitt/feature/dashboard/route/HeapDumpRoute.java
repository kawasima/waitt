package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import com.sun.management.HotSpotDiagnosticMXBean;
import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.gridkit.jvmtool.heapdump.StringCollector;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

public class HeapDumpRoute implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("heapdump", heapDump());
        return new Gson().toJson(attributes);
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
            Heap heap = HeapFactory.createFastHeap(dumpFile, 0);
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

}
