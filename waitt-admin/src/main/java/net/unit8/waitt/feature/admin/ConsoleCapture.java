package net.unit8.waitt.feature.admin;

import net.unit8.waitt.api.observability.LogLine;
import net.unit8.waitt.api.observability.TraceStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Tees {@code System.out}/{@code System.err} into a {@link LogBuffer} and the
 * {@link EventBroadcaster}, while writing every byte through to the original
 * stream so the terminal still shows everything.
 * <p>
 * The original stream references are captured before replacement, so waitt's own
 * stderr logging cannot feed back into the capture. Bytes are assembled into
 * newline-delimited lines; each completed line becomes one {@link LogEntry}.
 *
 * @author kawasima
 */
public class ConsoleCapture {
    private static final Charset CONSOLE_CHARSET = Charset.defaultCharset();

    private final LogBuffer buffer;
    private final EventBroadcaster broadcaster;

    private PrintStream originalOut;
    private PrintStream originalErr;
    private boolean installed;

    public ConsoleCapture(LogBuffer buffer, EventBroadcaster broadcaster) {
        this.buffer = buffer;
        this.broadcaster = broadcaster;
    }

    public synchronized void install() {
        if (installed) {
            return;
        }
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(newTee(originalOut, "OUT"));
        System.setErr(newTee(originalErr, "ERR"));
        installed = true;
    }

    public synchronized void restore() {
        if (!installed) {
            return;
        }
        System.setOut(originalOut);
        System.setErr(originalErr);
        installed = false;
    }

    private PrintStream newTee(PrintStream delegate, String streamName) {
        // Encode with the console's own charset so the bytes forwarded to the
        // original stream match what the terminal expects (avoids mojibake on
        // non-UTF-8 consoles such as Windows MS932).
        try {
            return new PrintStream(new LineTeeOutputStream(delegate, streamName), true, CONSOLE_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            return delegate;
        }
    }

    private void emit(String streamName, String line) {
        long now = System.currentTimeMillis();
        LogEntry entry = new LogEntry(now, streamName, line);
        buffer.add(entry);
        // Correlate to the in-flight request when this line was written on a
        // request thread (no-op when tracing is off or no trace is current).
        TraceStore.getInstance().recordLog(new LogLine(now, streamName, line));
        if (broadcaster.hasSubscribers()) {
            broadcaster.publish("log", entry.toJSON().toJSONString());
        }
    }

    /**
     * Writes every byte through to the delegate and assembles complete lines.
     */
    private final class LineTeeOutputStream extends OutputStream {
        private final PrintStream delegate;
        private final String streamName;
        private final ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(128);

        LineTeeOutputStream(PrintStream delegate, String streamName) {
            this.delegate = delegate;
            this.streamName = streamName;
        }

        @Override
        public synchronized void write(int b) {
            delegate.write(b);
            appendByte((byte) b);
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            delegate.write(b, off, len);
            for (int i = 0; i < len; i++) {
                appendByte(b[off + i]);
            }
        }

        private void appendByte(byte b) {
            if (b == '\n') {
                flushLine();
            } else {
                lineBuf.write(b);
            }
        }

        private void flushLine() {
            byte[] raw = lineBuf.toByteArray();
            lineBuf.reset();
            int len = raw.length;
            if (len > 0 && raw[len - 1] == '\r') {
                len--; // strip trailing CR from CRLF line endings
            }
            emit(streamName, new String(raw, 0, len, CONSOLE_CHARSET));
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }
    }
}
