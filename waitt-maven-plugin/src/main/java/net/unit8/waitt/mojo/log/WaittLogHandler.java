package net.unit8.waitt.mojo.log;

import org.apache.maven.plugin.logging.Log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 *
 * @author kawasima
 */
public class WaittLogHandler extends StreamHandler {
    public WaittLogHandler(Log mavenLogger) {
        setOutputStream(System.err);
        setFilter(new WaittLogFilter());

        if (mavenLogger.isDebugEnabled()) {
            setLevel(Level.ALL);
        } else if (mavenLogger.isInfoEnabled()) {
            setLevel(Level.INFO);
        } else if (mavenLogger.isWarnEnabled()) {
            setLevel(Level.WARNING);
        } else if (mavenLogger.isErrorEnabled()) {
            setLevel(Level.SEVERE);
        }
        setFormatter(new WaittFormatter());
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    /**
     * Override <tt>StreamHandler.close</tt> to do a flush but not
     * to close the output stream.  That is, we do <b>not</b>
     * close <tt>System.err</tt>.
     */
    @Override
    public void close() {
        flush();
    }
}
