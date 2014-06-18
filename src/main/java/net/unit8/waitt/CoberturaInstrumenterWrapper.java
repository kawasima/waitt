package net.unit8.waitt;

import net.sourceforge.cobertura.instrument.CoberturaInstrumenter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author kawasima
 */
public class CoberturaInstrumenterWrapper extends CoberturaInstrumenter implements Instrumenter {
    public byte[] instrumentClassByte(InputStream is)
            throws IOException {
        CoberturaInstrumenter.InstrumentationResult result = super.instrumentClass(is);
        return result == null ? null : result.getContent();
    }

}
