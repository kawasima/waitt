package net.unit8.waitt.feature.tracer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class TeeWriter extends PrintWriter {
    private static final Logger LOG = Logger.getLogger(TeeWriter.class.getName());
    private final Writer branch;

    public TeeWriter(Writer master, Writer branch) {
        super(master);
        this.branch = branch;
    }

    @Override
    public void write(char buf[], int off, int len) {
        super.write(buf, off, len);
        try {
            branch.write(buf, off, len);
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to write to branch writer", e);
        }
    }

    @Override
    public void flush() {
        super.flush();
        try {
            branch.flush();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to write to branch writer", e);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            branch.close();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Failed to write to branch writer", e);
        }
    }
}
