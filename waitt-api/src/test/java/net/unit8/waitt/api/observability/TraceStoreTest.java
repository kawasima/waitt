package net.unit8.waitt.api.observability;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class TraceStoreTest {

    private final TraceStore store = TraceStore.getInstance();

    @Before
    public void setUp() {
        store.clear();
        store.setCapacity(100);
        store.setListener(null);
        store.setEnabled(true);
    }

    @After
    public void tearDown() {
        store.setEnabled(false);
        store.setListener(null);
        store.clear();
        store.clearCurrentTraceId();
    }

    @Test
    public void disabledStoreDoesNotBeginOrRecord() {
        store.setEnabled(false);
        assertNull(store.beginTrace("t1", 1000L));
        store.setCurrentTraceId("t1");
        store.recordSql(new SqlEvent(0, 1, "select 1", 1, true, null));
        assertNull(store.getTrace("t1"));
    }

    @Test
    public void beginAddSpanCompleteMovesToRing() {
        store.beginTrace("t1", 1000L);
        store.addSpan("t1", span("s1", null, "HTTP GET"));
        store.completeTrace("t1");

        assertEquals(1, store.recentTraces().size());
        RequestTrace t = store.getTrace("t1");
        assertNotNull(t);
        assertEquals(1, t.getSpans().size());
    }

    @Test
    public void currentTraceIdCorrelatesSqlAndLogs() {
        store.beginTrace("t1", 1000L);
        store.setCurrentTraceId("t1");
        store.recordSql(new SqlEvent(0, 3, "select 1", 1, true, null));
        store.recordLog(new LogLine(0, "OUT", "hello"));
        store.clearCurrentTraceId();

        RequestTrace t = store.getTrace("t1");
        assertEquals(1, t.getSqlEvents().size());
        assertEquals(1, t.getLogs().size());
        // With no current trace bound, records are dropped.
        store.recordSql(new SqlEvent(0, 1, "select 2", 0, true, null));
        assertEquals(1, t.getSqlEvents().size());
    }

    @Test
    public void capacityEvictsOldestCompletedTrace() {
        store.setCapacity(2);
        for (int i = 1; i <= 3; i++) {
            store.beginTrace("t" + i, i);
            store.completeTrace("t" + i);
        }
        assertEquals(2, store.recentTraces().size());
        assertNull(store.getTrace("t1")); // evicted
        assertNotNull(store.getTrace("t3"));
        // recentTraces is newest-first.
        assertEquals("t3", store.recentTraces().get(0).getTraceId());
    }

    @Test
    public void listenerFiresOnComplete() {
        final AtomicReference<String> notified = new AtomicReference<String>();
        store.setListener(new TraceStore.TraceListener() {
            @Override
            public void onTraceCompleted(RequestTrace trace) {
                notified.set(trace.getTraceId());
            }
        });
        store.beginTrace("t1", 1000L);
        store.completeTrace("t1");
        assertEquals("t1", notified.get());
    }

    private static SpanRecord span(String id, String parent, String name) {
        return new SpanRecord(id, parent, name, 0L, 1_000_000L, false,
                Collections.<String, String>emptyMap());
    }
}
