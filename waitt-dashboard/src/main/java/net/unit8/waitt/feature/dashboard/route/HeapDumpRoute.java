package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import com.sun.management.HotSpotDiagnosticMXBean;
import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.gridkit.jvmtool.heapdump.StringCollector;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.unit8.waitt.feature.dashboard.Route;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

public class HeapDumpRoute implements Route {
    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        try {
            attributes.put("heapdump", heapDump());
        } catch (Exception e) {
            attributes.put("heapdump", Collections.emptyList());
            attributes.put("error", e.getMessage());
        }
        return new Gson().toJson(attributes);
    }

    private synchronized List<Map<String, Object>> heapDump() throws IOException {
        File heapDumpDirectory = new File("target");
        if (!heapDumpDirectory.exists()) {
            heapDumpDirectory.mkdirs();
        }
        File dumpFile = null;
        try {
            // createTempFile reserves a unique name; delete immediately so dumpHeap can write to it
            // (dumpHeap requires the target file to not exist)
            dumpFile = File.createTempFile("waitt", ".hprof", heapDumpDirectory);
            dumpFile.delete();
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap(dumpFile.getPath(), true);
            Heap heap = HeapFactory.createFastHeap(dumpFile, 0);
            StringCollector collector = new StringCollector();
            HeapHistogram histo = new HeapHistogram();
            collector.collect(heap, histo);
            List<HeapHistogram.ClassRecord> ht = new ArrayList<HeapHistogram.ClassRecord>(histo.getHisto());
            ht.add(collector.asClassRecord());
            Collections.sort(ht, HeapHistogram.BY_SIZE);
            List<HeapHistogram.ClassRecord> top = ht.subList(0, Math.min(ht.size(), 500));
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (HeapHistogram.ClassRecord r : top) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put("className", r.getClassName());
                row.put("instanceCount", r.getInstanceCount());
                row.put("totalSize", r.getTotalSize());
                result.add(row);
            }
            return result;
        } finally {
            if (dumpFile != null && dumpFile.exists()) {
                dumpFile.delete();
            }
        }
    }

}
