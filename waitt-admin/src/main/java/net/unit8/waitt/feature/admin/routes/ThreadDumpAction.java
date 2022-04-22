package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONArray;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class ThreadDumpAction implements Route {
    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/thread".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        json.put("threads", threadDump());
        ResponseUtils.responseJSON(exchange, json);
    }

    private JSONArray threadDump() {
        JSONArray threadDump = new JSONArray();
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = mxBean.dumpAllThreads(true, true);
        for (ThreadInfo info : threadInfos) {
            if (info.getThreadName().startsWith("admin-server-")) {
                continue;
            }
            JSONObject obj = new JSONObject();
            obj.put("threadName", info.getThreadName());
            obj.put("threadId", info.getThreadId());
            obj.put("threadState", info.getThreadState());
            obj.put("blockedCount", info.getBlockedCount());
            obj.put("blockedTime", info.getBlockedTime());
            obj.put("waitedCount", info.getWaitedCount());
            obj.put("waitedTime", info.getWaitedTime());
            obj.put("lockName", info.getLockName());
            obj.put("lockOwnerId", info.getLockOwnerId());
            obj.put("lockOwnerName", info.getLockOwnerName());
            obj.put("isInNative", info.isInNative());
            obj.put("isSuspend", info.isSuspended());
            obj.put("stackTrace", stacktrace(info.getStackTrace()));
            threadDump.add(obj);
        }
        return threadDump;
    }

    private JSONArray stacktrace(StackTraceElement[] stackTraceElements) {
        JSONArray stackTraces = new JSONArray();
        for (StackTraceElement el : stackTraceElements) {
            JSONObject stackTrace = new JSONObject();
            stackTrace.put("className", el.getClassName());
            stackTrace.put("methodName", el.getMethodName());
            stackTrace.put("fileName", el.getFileName());
            stackTrace.put("lineNumber", el.getLineNumber());
            stackTraces.add(stackTrace);
        }
        return stackTraces;
    }
}
