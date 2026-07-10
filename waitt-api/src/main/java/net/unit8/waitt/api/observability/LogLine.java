package net.unit8.waitt.api.observability;

/**
 * One console line correlated to a request. Recorded into the shared
 * {@link TraceStore} from the console capture running on the request thread.
 *
 * @author kawasima
 */
public final class LogLine {
    private final long timestampMillis;
    private final String stream;
    private final String text;

    public LogLine(long timestampMillis, String stream, String text) {
        this.timestampMillis = timestampMillis;
        this.stream = stream;
        this.text = text;
    }

    public long getTimestampMillis() { return timestampMillis; }
    /** Source of the line, e.g. {@code OUT} or {@code ERR}. */
    public String getStream() { return stream; }
    public String getText() { return text; }
}
