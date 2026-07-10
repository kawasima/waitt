package net.unit8.waitt.feature.admin;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LogBufferTest {

    @Test
    public void snapshotPreservesInsertionOrder() {
        LogBuffer buffer = new LogBuffer(10);
        buffer.add(new LogEntry(1, "OUT", "a"));
        buffer.add(new LogEntry(2, "OUT", "b"));
        buffer.add(new LogEntry(3, "ERR", "c"));

        List<LogEntry> snapshot = buffer.snapshot();
        assertEquals(3, snapshot.size());
        assertEquals("a", snapshot.get(0).text);
        assertEquals("b", snapshot.get(1).text);
        assertEquals("c", snapshot.get(2).text);
    }

    @Test
    public void appendPastCapacityEvictsOldest() {
        LogBuffer buffer = new LogBuffer(3);
        buffer.add(new LogEntry(1, "OUT", "1"));
        buffer.add(new LogEntry(2, "OUT", "2"));
        buffer.add(new LogEntry(3, "OUT", "3"));
        buffer.add(new LogEntry(4, "OUT", "4"));

        List<LogEntry> snapshot = buffer.snapshot();
        assertEquals(3, snapshot.size());
        assertEquals("2", snapshot.get(0).text);
        assertEquals("3", snapshot.get(1).text);
        assertEquals("4", snapshot.get(2).text);
    }

    @Test
    public void capacityIsAtLeastOne() {
        LogBuffer buffer = new LogBuffer(0);
        buffer.add(new LogEntry(1, "OUT", "x"));
        buffer.add(new LogEntry(2, "OUT", "y"));

        List<LogEntry> snapshot = buffer.snapshot();
        assertEquals(1, snapshot.size());
        assertEquals("y", snapshot.get(0).text);
    }
}
