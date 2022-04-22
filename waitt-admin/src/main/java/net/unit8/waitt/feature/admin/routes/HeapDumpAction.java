package net.unit8.waitt.feature.admin.routes;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONArray;
import net.unit8.waitt.feature.admin.json.JSONObject;
import org.gridkit.jvmtool.heapdump.HeapHistogram;
import org.gridkit.jvmtool.heapdump.StringCollector;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeapDumpAction  implements Route {
    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/heap".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        json.put("heapdump", heapDump());
        ResponseUtils.responseJSON(exchange, json);
    }

    private JSONArray heapDump() throws IOException {
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
            List<HeapHistogram.ClassRecord> top500 = ht.subList(0, 500);

            JSONArray records = new JSONArray();
            for (HeapHistogram.ClassRecord r : top500) {
                JSONObject record = new JSONObject();
                record.put("className", r.getClassName());
                record.put("instanceCount", r.getInstanceCount());
                record.put("totalSize", r.getTotalSize());
                records.add(record);
            }
            return records;
        } finally {
            if (dumpFile != null && dumpFile.exists()) {
                dumpFile.delete();
            }
        }
    }
}
