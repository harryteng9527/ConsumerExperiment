import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

public class Listener implements ConsumerRebalanceListener {
    private long revokedTime = System.currentTimeMillis();
    private final KafkaConsumer<String, String> consumer;
    private final int id;
    private ArrayList<DownTime> downTimes;
    public Listener(KafkaConsumer<String, String> consumer, int id) {
        this.consumer = consumer;
        this.id = id;
        this.downTimes = new ArrayList<>();
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        System.out.println("Revoked #" + id);
        revokedTime = System.currentTimeMillis();
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        System.out.println("Assigned #" + id);
        long assignedTime = System.currentTimeMillis();
        Duration duration = Duration.ofMillis(assignedTime - revokedTime);
        DownTime downTime = new DownTime(duration, consumer.groupMetadata().generationId());
        downTimes.add(downTime);
        Utility.timeMap.put(id, downTimes);
    }
    @Override
    public void onPartitionsLost(Collection<TopicPartition> partitions){
        System.out.println("consumer #" + id + " in lost");
    }
}
