import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Create multiple thread consumers
 * */
public class MultipleThreadConsumer extends Thread{
    private final int id;
    private final KafkaConsumer<String, String> consumer;
    private final Listener listener;

    public MultipleThreadConsumer(int id, KafkaConsumer<String, String> consumer) {
        this.id = id;
        this.consumer = consumer;
        this.listener = new Listener(consumer, id);
    }

    public KafkaConsumer<String, String> getConsumer() { return consumer; }

    public void doSubscribe(Set<String> topics) {
        consumer.subscribe(topics, listener);
    }

    public void doSubscribe(Pattern pattern) {
        consumer.subscribe(pattern, listener);
    }

    /**
     * Doesn't support multi-thread access
     * */
    public void unsubscribe() {
        consumer.unsubscribe();
    }

    @Override
    public void run() {
        System.out.println("Consumer #" + id + " has start");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                consumer.poll(Duration.ofSeconds(1));
            }
        } catch (Exception e) {
            System.out.println("Close consumer #" + id);
            Thread.interrupted();
        } finally {
            consumer.close();
        }
    }
}
