package net.unit8.waitt.feature.admin.json;

import java.io.IOException;
import java.io.Writer;

public interface JSONStreamAware {
    /**
     * write JSON string to out.
     */
    void writeJSONString(Writer out) throws IOException;
}
