package net.unit8.waitt.feature.tracer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author kawasima
 */
public class TeeWriter extends PrintWriter {
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
        } catch(IOException ignore) {
        }
    }

    @Override
    public void flush() {
        super.flush();
        try {
            branch.flush();
        } catch (IOException ignore) {

        }
    }

    @Override
    public void close() {
        super.close();
        try {
            branch.close();
        } catch(IOException ignore) {

        }
    }
}
