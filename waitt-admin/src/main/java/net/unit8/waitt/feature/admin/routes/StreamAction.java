package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.EventBroadcaster;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;

/**
 * Server-Sent Events stream of live {@code request}, {@code log}, and
 * {@code metrics} events.
 * <p>
 * The handler thread stays parked on this connection for its lifetime, draining
 * the subscriber queue and writing frames. It returns only when the client
 * disconnects (a write {@link IOException}), at which point
 * {@code AdminApplication} closes the exchange. Concurrent streams are capped so
 * a runaway set of browser tabs cannot exhaust the admin thread pool.
 *
 * @author kawasima
 */
public class StreamAction implements Route {
    /** Maximum concurrent SSE connections; extra requests get 503. */
    static final int MAX_STREAMS = 8;
    /** Idle timeout after which a heartbeat comment is written. */
    private static final long HEARTBEAT_MILLIS = 15000L;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final EventBroadcaster broadcaster;

    public StreamAction(EventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/stream".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Atomically claim a slot so concurrent connects can't exceed the cap.
        EventBroadcaster.Subscriber subscriber = broadcaster.subscribeIfBelow(MAX_STREAMS);
        if (subscriber == null) {
            exchange.sendResponseHeaders(503, -1);
            return;
        }

        try {
            exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("text/event-stream; charset=utf-8"));
            exchange.getResponseHeaders().put("Cache-Control", Collections.singletonList("no-cache"));
            exchange.getResponseHeaders().put("Connection", Collections.singletonList("keep-alive"));
            ResponseUtils.applyCorsOrigin(exchange);
            exchange.sendResponseHeaders(200, 0); // 0 -> chunked, keep the response open

            OutputStream os = exchange.getResponseBody();
            write(os, ": connected\n\n"); // initial comment so the client's onopen fires
            while (true) {
                EventBroadcaster.Event event = subscriber.poll(HEARTBEAT_MILLIS);
                if (event == null) {
                    write(os, ": ping\n\n"); // heartbeat / dead-connection detection
                } else {
                    write(os, "event: " + event.type + "\ndata: " + event.data + "\n\n");
                }
            }
        } catch (IOException disconnected) {
            // Client went away; fall through to unsubscribe.
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } finally {
            broadcaster.unsubscribe(subscriber);
        }
    }

    private void write(OutputStream os, String frame) throws IOException {
        os.write(frame.getBytes(UTF8));
        os.flush();
    }
}
