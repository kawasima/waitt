package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide information about the server.
 *
 * @author kawasima
 */
public class ServerAction implements Route {
    private final EmbeddedServer server;
    private final String rrdPath;

    public ServerAction(EmbeddedServer server, String rrdPath) {
        this.server = server;
        this.rrdPath = rrdPath;
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && exchange.getRequestURI().getPath().startsWith("/server");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("/server/cpu.png".equals(exchange.getRequestURI().getPath())) {
            byte[] graph = renderGraph(GraphType.CPU);
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, graph.length);
            exchange.getResponseBody().write(graph);
        } else if ("/server/memory.png".equals(exchange.getRequestURI().getPath())) {
            byte[] graph = renderGraph(GraphType.MEMORY);
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, graph.length);
            exchange.getResponseBody().write(graph);
        } else {
            JSONObject json = new JSONObject();
            JSONObject serverMetadata = new JSONObject();

            serverMetadata.put("name", server.getName());
            serverMetadata.put("status", server.getStatus().name());

            json.put("serverMetadata", serverMetadata);

            // JVM metrics
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            json.put("uptime", runtime.getUptime());
            json.put("startTime", runtime.getStartTime());

            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memory.getHeapMemoryUsage();
            JSONObject heapJson = new JSONObject();
            heapJson.put("used", heap.getUsed());
            heapJson.put("committed", heap.getCommitted());
            heapJson.put("max", heap.getMax());
            json.put("heap", heapJson);

            MemoryUsage nonHeap = memory.getNonHeapMemoryUsage();
            JSONObject nonHeapJson = new JSONObject();
            nonHeapJson.put("used", nonHeap.getUsed());
            nonHeapJson.put("committed", nonHeap.getCommitted());
            nonHeapJson.put("max", nonHeap.getMax());
            json.put("nonHeap", nonHeapJson);

            ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();
            json.put("loadedClassCount", classLoading.getLoadedClassCount());
            json.put("totalLoadedClassCount", classLoading.getTotalLoadedClassCount());
            json.put("unloadedClassCount", classLoading.getUnloadedClassCount());

            List<JSONObject> gcList = new ArrayList<JSONObject>();
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                JSONObject gcJson = new JSONObject();
                gcJson.put("name", gc.getName());
                gcJson.put("collectionCount", gc.getCollectionCount());
                gcJson.put("collectionTime", gc.getCollectionTime());
                gcList.add(gcJson);
            }
            json.put("gc", gcList);

            ResponseUtils.responseJSON(exchange, json);
        }
    }

    private byte[] renderGraph(GraphType type) throws IOException {
        RrdGraphDef gDef = new RrdGraphDef();
        long now = Util.getTimestamp();

        gDef.setStartTime(now - 24 * 60 * 60);
        gDef.setEndTime(now);
        gDef.setWidth(500);
        gDef.setHeight(300);
        gDef.setFilename("-");
        gDef.setPoolUsed(true);
        gDef.setImageFormat("png");

        switch (type) {
            case MEMORY:
                renderGraphMemory(gDef);
                break;
            case CPU:
                renderGraphCpu(gDef);
                break;
        }

        return new RrdGraph(gDef).getRrdGraphInfo().getBytes();
    }

    private void renderGraphMemory(RrdGraphDef gDef) {
        gDef.setTitle("Memory usage");
        gDef.setVerticalLabel("bytes");
        gDef.line("Free memory (physical)", Color.BLUE);
        gDef.line("Free memory (swap)", Color.GREEN);
        gDef.datasource("Free memory (physical)", rrdPath, "memory-physical", ConsolFun.AVERAGE);
        gDef.datasource("Free memory (swap)",     rrdPath, "memory-swap",     ConsolFun.AVERAGE);
        gDef.print("Free memory (physical)", ConsolFun.AVERAGE, "Free memory (physical) = %.3f%s");
        gDef.print("Free memory (swap)", ConsolFun.AVERAGE, "Free memory (swap) = %.3f%s\\c");
    }

    private void renderGraphCpu(RrdGraphDef gDef) {
        gDef.setTitle("CPU load");
        gDef.setVerticalLabel("%");
        gDef.line("Process", Color.BLUE);
        gDef.line("System", Color.GREEN);
        gDef.datasource("Process", rrdPath, "load-process", ConsolFun.AVERAGE);
        gDef.datasource("System",  rrdPath, "load-system",  ConsolFun.AVERAGE);
        gDef.gprint("Process", ConsolFun.AVERAGE, "Load average (process) = %.3f%s");
        gDef.gprint("System", ConsolFun.AVERAGE, "Load average (system) = %.3f%s\\c");
    }

    private enum GraphType { MEMORY, CPU }

}
