package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.unit8.waitt.feature.dashboard.Route;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadDumpRoute implements Route {
    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("threads", threadDump());
        response.setContentType("application/json");
        return new Gson().toJson(attributes);
    }

    private List<Map<String, Object>> threadDump() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = mxBean.dumpAllThreads(true, true);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ThreadInfo ti : threadInfos) {
            Map<String, Object> t = new HashMap<>();
            t.put("threadId", ti.getThreadId());
            t.put("threadName", ti.getThreadName());
            t.put("threadState", ti.getThreadState().name());
            t.put("blockedCount", ti.getBlockedCount());
            t.put("blockedTime", ti.getBlockedTime());
            t.put("waitedCount", ti.getWaitedCount());
            t.put("waitedTime", ti.getWaitedTime());
            t.put("lockName", ti.getLockName());
            t.put("lockOwnerId", ti.getLockOwnerId());
            t.put("lockOwnerName", ti.getLockOwnerName());
            t.put("inNative", ti.isInNative());
            t.put("suspended", ti.isSuspended());
            List<String> stack = new ArrayList<>();
            for (StackTraceElement ste : ti.getStackTrace()) {
                stack.add(ste.toString());
            }
            t.put("stackTrace", stack);
            result.add(t);
        }
        return result;
    }
}
