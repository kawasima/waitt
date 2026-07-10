package net.unit8.waitt.feature.admin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Fixed-capacity ring buffer of recent console lines. When full, appending a new
 * entry evicts the oldest. Snapshots are returned oldest to newest.
 *
 * @author kawasima
 */
public class LogBuffer {
    private final int capacity;
    private final Deque<LogEntry> entries;

    public LogBuffer(int capacity) {
        this.capacity = capacity < 1 ? 1 : capacity;
        // Grow lazily; the ring is bounded by add()'s own size check, so we must
        // not preallocate a backing array sized to a (possibly huge) capacity.
        this.entries = new ArrayDeque<LogEntry>();
    }

    public synchronized void add(LogEntry entry) {
        if (entries.size() >= capacity) {
            entries.pollFirst();
        }
        entries.addLast(entry);
    }

    /** Snapshot of the buffered entries, oldest first. */
    public synchronized List<LogEntry> snapshot() {
        return new ArrayList<LogEntry>(entries);
    }

    public synchronized int size() {
        return entries.size();
    }
}
