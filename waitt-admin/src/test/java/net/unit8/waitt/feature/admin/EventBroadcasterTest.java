package net.unit8.waitt.feature.admin;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventBroadcasterTest {

    private final EventBroadcaster broadcaster = EventBroadcaster.getInstance();

    @After
    public void tearDown() {
        broadcaster.shutdown();
    }

    @Test
    public void deliversPublishedEventToSubscriber() throws Exception {
        EventBroadcaster.Subscriber subscriber = broadcaster.subscribe();
        broadcaster.publish("log", "{\"text\":\"hello\"}");

        EventBroadcaster.Event event = subscriber.poll(1000);
        assertNotNull(event);
        assertEquals("log", event.type);
        assertEquals("{\"text\":\"hello\"}", event.data);
    }

    @Test
    public void unsubscribeStopsDelivery() throws Exception {
        EventBroadcaster.Subscriber subscriber = broadcaster.subscribe();
        broadcaster.unsubscribe(subscriber);
        broadcaster.publish("log", "{}");

        assertNull(subscriber.poll(50));
    }

    @Test
    public void publishDoesNotBlockAndDropsOldestWhenFull() throws Exception {
        EventBroadcaster.Subscriber subscriber = broadcaster.subscribe();
        int overflow = EventBroadcaster.QUEUE_CAPACITY + 50;

        // Publishing far past capacity without draining must not block the producer.
        for (int i = 0; i < overflow; i++) {
            broadcaster.publish("log", Integer.toString(i));
        }

        // Exactly QUEUE_CAPACITY events are retained; the oldest were dropped,
        // so the first retained is event #50.
        EventBroadcaster.Event first = subscriber.poll(50);
        assertNotNull(first);
        assertEquals(Integer.toString(overflow - EventBroadcaster.QUEUE_CAPACITY), first.data);

        int retained = 1;
        while (subscriber.poll(10) != null) {
            retained++;
        }
        assertEquals(EventBroadcaster.QUEUE_CAPACITY, retained);
    }

    @Test
    public void publishWithNoSubscribersIsNoop() {
        broadcaster.publish("log", "{}"); // must not throw
        assertEquals(0, broadcaster.subscriberCount());
    }

    @Test
    public void subscribeIfBelowEnforcesCapAtomically() {
        assertNotNull(broadcaster.subscribeIfBelow(2));
        assertNotNull(broadcaster.subscribeIfBelow(2));
        assertNull(broadcaster.subscribeIfBelow(2)); // at cap
        assertEquals(2, broadcaster.subscriberCount());
    }
}
