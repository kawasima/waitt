package net.unit8.waitt.feature.admin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

public class ConsoleCaptureTest {

    private PrintStream savedOut;
    private PrintStream savedErr;
    private ByteArrayOutputStream downstream;
    private ConsoleCapture capture;
    private LogBuffer buffer;

    @Before
    public void setUp() {
        savedOut = System.out;
        savedErr = System.err;
        downstream = new ByteArrayOutputStream();
        // Install a known original stream so we can assert write-through.
        System.setOut(new PrintStream(downstream));
        buffer = new LogBuffer(100);
        capture = new ConsoleCapture(buffer, EventBroadcaster.getInstance());
        capture.install();
    }

    @After
    public void tearDown() {
        capture.restore();
        System.setOut(savedOut);
        System.setErr(savedErr);
    }

    @Test
    public void assemblesCompleteLine() {
        System.out.print("hello world\n");
        List<LogEntry> logs = buffer.snapshot();
        assertEquals(1, logs.size());
        assertEquals("hello world", logs.get(0).text);
        assertEquals("OUT", logs.get(0).stream);
    }

    @Test
    public void assemblesLineSplitAcrossWrites() {
        System.out.print("abc");
        System.out.print("def\n");
        List<LogEntry> logs = buffer.snapshot();
        assertEquals(1, logs.size());
        assertEquals("abcdef", logs.get(0).text);
    }

    @Test
    public void trailingPartialLineIsNotEmittedUntilNewline() {
        System.out.print("no newline yet");
        assertEquals(0, buffer.snapshot().size());
        System.out.print("\n");
        assertEquals(1, buffer.snapshot().size());
    }

    @Test
    public void stripsCarriageReturn() {
        System.out.print("windows line\r\n");
        assertEquals("windows line", buffer.snapshot().get(0).text);
    }

    @Test
    public void writesThroughToOriginalStream() {
        System.out.print("passthrough\n");
        assertTrue(downstream.toString().contains("passthrough"));
    }

    @Test
    public void restoreReinstatesOriginalStream() {
        PrintStream duringCapture = System.out;
        capture.restore();
        assertNotSame(duringCapture, System.out);
    }
}
