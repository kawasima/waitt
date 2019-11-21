package net.unit8.waitt.feature.dashboard.route;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadDumpRoute implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("threads", threadDump());
        response.type("application/json");
        return new Gson().toJson(attributes);
    }

    private List<ThreadInfo> threadDump() {
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = mxBean.dumpAllThreads(true, true);
        return Arrays.asList(threadInfos);
    }
}
