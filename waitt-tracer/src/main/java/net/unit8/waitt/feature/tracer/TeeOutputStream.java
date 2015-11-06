package net.unit8.waitt.feature.tracer;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author kawasima
 */
public class TeeOutputStream extends ServletOutputStream {
    private final OutputStream o1;
    private final OutputStream o2;

    public TeeOutputStream(OutputStream o1, OutputStream o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    @Override
    public void write(int b) throws IOException {
        o1.write(b);
        o2.write(b);
    }
}
