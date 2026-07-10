package net.unit8.waitt.feature.admin;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process publish/subscribe hub feeding the Server-Sent Events endpoint.
 * <p>
 * Producers (request log, console capture, metrics ticker) call
 * {@link #publish(String, String)}; each open SSE connection is a
 * {@link Subscriber} draining its own bounded queue. Publishing never blocks a
 * producer: when a subscriber's queue is full the oldest event is dropped, since
 * development-time observability data is loss-tolerant and a slow browser must
 * never slow the application.
 *
 * @author kawasima
 */
public class EventBroadcaster {
    private static final EventBroadcaster INSTANCE = new EventBroadcaster();

    /** Per-subscriber queue capacity before the oldest event is dropped. */
    static final int QUEUE_CAPACITY = 256;

    private final Set<Subscriber> subscribers = ConcurrentHashMap.newKeySet();
    private final Object subscribeLock = new Object();

    private EventBroadcaster() {
    }

    public static EventBroadcaster getInstance() {
        return INSTANCE;
    }

    /**
     * One event: a Server-Sent Events {@code type} and a pre-serialized JSON
     * {@code data} payload (already escaped to a single line).
     */
    public static final class Event {
        public final String type;
        public final String data;

        public Event(String type, String data) {
            this.type = type;
            this.data = data;
        }
    }

    /** A single SSE connection draining events from its own bounded queue. */
    public static final class Subscriber {
        private final BlockingQueue<Event> queue = new ArrayBlockingQueue<Event>(QUEUE_CAPACITY);

        public Event poll(long timeoutMillis) throws InterruptedException {
            return queue.poll(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        void offerDroppingOldest(Event event) {
            while (!queue.offer(event)) {
                queue.poll();
            }
        }
    }

    public Subscriber subscribe() {
        Subscriber subscriber = new Subscriber();
        subscribers.add(subscriber);
        return subscriber;
    }

    /**
     * Atomically subscribe only if the current count is below {@code max};
     * returns {@code null} when the cap is already reached. This closes the
     * check-then-act race a separate count-then-subscribe would leave open.
     */
    public Subscriber subscribeIfBelow(int max) {
        synchronized (subscribeLock) {
            if (subscribers.size() >= max) {
                return null;
            }
            return subscribe();
        }
    }

    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public int subscriberCount() {
        return subscribers.size();
    }

    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    public void publish(String type, String jsonData) {
        if (subscribers.isEmpty()) {
            return;
        }
        Event event = new Event(type, jsonData);
        for (Subscriber subscriber : subscribers) {
            subscriber.offerDroppingOldest(event);
        }
    }

    /** Drop all subscribers (called on admin server shutdown). */
    public void shutdown() {
        subscribers.clear();
    }
}
