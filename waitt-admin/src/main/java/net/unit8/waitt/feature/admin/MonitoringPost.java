package net.unit8.waitt.feature.admin;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;

/**
 * @author kawasima
 */
public class MonitoringPost implements Runnable {
    private final String rrdPath;

    public MonitoringPost(String rrdPath) {
        this.rrdPath = rrdPath;
    }

    protected RrdDef createRrdDef() {
        RrdDef def = new RrdDef(rrdPath, 300);
        def.addArchive(ConsolFun.AVERAGE, 0.5, 1, 288);
        def.addArchive(ConsolFun.AVERAGE, 0.5, 3, 672);
        def.addArchive(ConsolFun.AVERAGE, 0.5, 12, 744);
        def.addArchive(ConsolFun.AVERAGE, 0.5, 72, 1480);
        def.addDatasource("load-process",    DsType.GAUGE, 300, 0, Double.NaN);
        def.addDatasource("load-system",     DsType.GAUGE, 300, 0, Double.NaN);
        def.addDatasource("memory-physical", DsType.GAUGE, 300, 0, Double.NaN);
        def.addDatasource("memory-swap",     DsType.GAUGE, 300, 0, Double.NaN);
        return def;
    }

    protected RrdDb getRrdDb() throws IOException {
        File rrdFile = new File(rrdPath);
        if (!rrdFile.exists()) {
            if (!rrdFile.getParentFile().exists() && !rrdFile.getParentFile().mkdirs()) {
                throw new IOException("Can't create rrd " + rrdPath);
            }
            return RrdDbPool.getInstance().requestRrdDb(createRrdDef());

        } else {
            return RrdDbPool.getInstance().requestRrdDb(rrdPath);
        }

    }

    protected void update() throws IOException {
        OperatingSystemMXBean mx = ManagementFactory.getOperatingSystemMXBean();
        if (mx instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunMx = (com.sun.management.OperatingSystemMXBean) mx;
            RrdDb db = getRrdDb();

            try {
                Sample sample = db.createSample(Util.getTimestamp());
                sample.setValue("load-process", sunMx.getProcessCpuLoad());
                sample.setValue("load-system", sunMx.getSystemCpuLoad());
                sample.setValue("memory-physical", sunMx.getFreePhysicalMemorySize());
                sample.setValue("memory-swap", sunMx.getFreeSwapSpaceSize());
                sample.update();
            } finally {
                RrdDbPool.getInstance().release(db);
            }
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                update();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }

            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
