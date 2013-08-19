package net.unit8.waitt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * NullOutputStream
 */
public class NullOutputStream extends OutputStream {
    public static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

    public void write(byte[] b, int off, int len) {

    }

    public void write(int b) {

    }

    public void write(byte[] b) throws IOException {

    }
}
