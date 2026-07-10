package net.unit8.waitt.feature.admin;

import net.unit8.waitt.feature.admin.json.JSONObject;

/**
 * One captured console line: when it was written, which stream it came from
 * ({@code OUT} or {@code ERR}), and the raw text (no trailing newline).
 *
 * @author kawasima
 */
public final class LogEntry {
    public final long timestamp;
    public final String stream;
    public final String text;

    public LogEntry(long timestamp, String stream, String text) {
        this.timestamp = timestamp;
        this.stream = stream;
        this.text = text;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("timestamp", timestamp);
        json.put("stream", stream);
        json.put("text", text);
        return json;
    }
}
