package net.unit8.waitt.feature.admin;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.feature.admin.routes.AppAction;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminServerTest {
    @Test
    public void test() throws IOException {
        final WebappConfiguration config = new WebappConfiguration();
        config.setApplicationName("test");
        config.setBaseDirectory(new File("/"));
        config.setSourceDirectory(new File("src/main/java"));
        final AppAction appAction = new AppAction(config);
        final HttpExchange exchange = mock(HttpExchange.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(baos);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        appAction.handle(exchange);
    }
}