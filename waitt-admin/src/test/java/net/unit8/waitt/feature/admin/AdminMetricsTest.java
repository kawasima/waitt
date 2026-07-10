package net.unit8.waitt.feature.admin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AdminMetricsTest {

    private AdminMetrics metrics;

    @Before
    public void setUp() {
        metrics = new AdminMetrics();
    }

    @After
    public void tearDown() {
        metrics.close();
    }

    @Test
    public void recordsHttpRequestWithMethodAndStatusTags() {
        metrics.recordHttpRequest("GET", 200, 12);

        String scrape = metrics.scrape();
        assertTrue(scrape.contains("http_server_requests_seconds_count"));
        assertTrue(scrape.contains("method=\"GET\""));
        assertTrue(scrape.contains("status=\"200\""));
    }

    @Test
    public void countsMultipleRequestsOnSameSeries() {
        metrics.recordHttpRequest("POST", 500, 5);
        metrics.recordHttpRequest("POST", 500, 7);

        String scrape = metrics.scrape();
        // Two requests on the {POST,500} series render a count of 2.0. Accept the
        // series with or without the trailing comma before '}' (differs by
        // Prometheus client format version).
        assertTrue(scrape.contains("http_server_requests_seconds_count{method=\"POST\",status=\"500\",} 2.0")
                || scrape.contains("http_server_requests_seconds_count{method=\"POST\",status=\"500\"} 2.0"));
    }

    @Test
    public void exposesJvmBinderMetrics() {
        String scrape = metrics.scrape();
        assertTrue(scrape.contains("jvm_memory_used_bytes"));
    }
}
