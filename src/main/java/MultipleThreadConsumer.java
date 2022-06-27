import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Create multiple thread consumers
 * */
public class MultipleThreadConsumer extends Thread {
    private final int id;
    private final KafkaConsumer<String, String> consumer;
    private final Listener listener;
    private boolean isEnforce;
    private boolean isUnsubscribe;

    public MultipleThreadConsumer(int id, KafkaConsumer<String, String> consumer) {
        this.id = id;
        this.consumer = consumer;
        this.listener = new Listener(consumer, id);
        isEnforce = false;
        isUnsubscribe = false;
    }

    public KafkaConsumer<String, String> getConsumer() {
        return consumer;
    }

    public void doSubscribe(Set<String> topics) {
        consumer.subscribe(topics, listener);
    }

    public void doSubscribe(Pattern pattern) {
        consumer.subscribe(pattern, listener);
    }

    /**
     * Doesn't support multi-thread access
     */
    private void unsubscribe() {
        consumer.unsubscribe();
    }

    private void enforce() {
        consumer.enforceRebalance();
    }

    public void setEnforce() {
        isEnforce = true;
    }

    public void setUnsubscribe() {
        isUnsubscribe = true;
    }

    @Override
    public void run() {
        System.out.println("Consumer #" + id + " has start");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                consumer.poll(Duration.ofSeconds(1));
                if (isEnforce) {
                    enforce();
                    isEnforce = false;
                } else if (isUnsubscribe) {
                    unsubscribe();
                    isUnsubscribe = false;
                    doSubscribe(parseTopic(Utility.findTopics()));
                }
            }
        } catch (Exception e) {
            System.out.println("Close consumer #" + id);
            Thread.interrupted();
        } finally {
            consumer.close();
        }
    }

    private Set<String> parseTopic(Set<String> topics) {
        ArrayList<String> list = new ArrayList<>();
        topics.forEach((v) -> {
            list.add(v);
        });

        topics.remove(list.get(0));
        topics.remove(list.get(2));
        return topics;
    }
}
