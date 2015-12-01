package net.unit8.waitt.feature.tracer.entry;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.text.DateFormat;

/**
 * @author kawasima
 */
public class ResponseEntryTest {
    Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Test
    public void test() {
        ResponseEntry response = new ResponseEntry("/", 200, "Hello");
    }
}
