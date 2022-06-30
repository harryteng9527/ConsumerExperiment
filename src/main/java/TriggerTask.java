import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The situations of trigger rebalance process
 * 1. a consumer / consumers leave consumer group(maybe shut down or leave group) -> ok
 * 2. a new consumer join into the consumer group -> ok
 * 3. partitions of the topics which one of the consumer of the consumer group that subscribe was increased
 * 4. for pattern subscribe, add a new topic into the Kafka cluster
 * */
public class TriggerTask implements AutoCloseable {
    private int range;
    private final Random randomGenerator;
    private final AdminClient adminClient;

    public TriggerTask(int range) {
        this.range = range;
        this.randomGenerator = new Random(System.currentTimeMillis());
        adminClient = Utility.createAdminClient();
    }

    public void killConsumers(ArrayList<MultipleThreadConsumer> consumers) throws InterruptedException {
        int size = consumers.size();
        for(int i = 0; i < size; i++) {
            consumers.get(i).interrupt();
            TimeUnit.SECONDS.sleep(10);
        }
    }

    /**
    *  specify the unlucky consumer to kill.
    * */
    public void killConsumer(ArrayList<MultipleThreadConsumer> consumers, int unlucky) {
        consumers.get(unlucky).interrupt();
    }

    /**
     * random kill the victim consumer.
     * */
    public void killConsumer(ArrayList<MultipleThreadConsumer> consumers) {
        int victim = selectVictim();
        System.out.println("Kill consumer #"+consumers.get(victim).getId());
        consumers.get(victim).interrupt();
        range -= 1;
    }

    /**
     * create a new consumer to consumer group and append it to the ArrayList<MultipleThreadConsumer>
     * */
    public void appendNewConsumer(ArrayList<MultipleThreadConsumer> consumers, int id, boolean isPattern) {
        MultipleThreadConsumer consumer = Utility.startUpConsumer(id, isPattern);
        consumers.add(consumer);
        consumer.start();
        range += 1;
    }

    /**
     * the method 'unsubscibe' doesn't support multi-thread access, so it not work.
     * */
    public void unsubscribeAndSubscribe(ArrayList<MultipleThreadConsumer> consumers) {
        int victim = selectVictim();
        Set<String> topics = queryTopics();
        consumers.get(victim).setUnsubscribe();
        System.out.println("consumer #"+ consumers.get(victim).getId() + "unsubscribe");
    }

    private Set<String> queryTopics() {
        Set<String> topics = null;
        try {
            topics = adminClient.listTopics().names().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topics;
    }
    /**
     * for re-subscribing
     * */
    private Set<String> selectTopic(Set<String> topics) {
        Set<String> select = new HashSet<>();
        for(int i = 0; i < 3; i++)
            select.add(topics.iterator().next());
        return select;
    }
    /**
     * just a helper method to select victim consumer.
     * @return the number id of the consumer
     */
    private int selectVictim() {
        return randomGenerator.nextInt(range);
    }

    private Map<String, Integer> describeTopic() {
        Set<String> topic = selectTopic();
        Map<String, Integer> topicPartition = new HashMap<>();
        try {
            var result = adminClient.describeTopics(topic).values();
            topicPartition.put(result.keySet().iterator().next(), result.values().iterator().next().get().partitions().size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return topicPartition;
    }
    /**
     * random select a topic for increase partition count.
     * */
    private Set<String> selectTopic() {
        Set<String> topics = Utility.findTopics();
        List<String> list = new ArrayList<>(topics);
        Set<String> topic = new HashSet<>();
        int victim = randomGenerator.nextInt(topics.size());

        System.out.println("victim " + victim);
        topic.add(list.get(victim));
        return topic;
    }

    public void modifyPartitionsCount() {
        Map<String, Integer> topicPartition = describeTopic();
        NewPartitions newPartitions = NewPartitions.increaseTo(topicPartition.values().iterator().next()+1);
        Map<String, NewPartitions> topic = new HashMap<>();
        topic.put(topicPartition.keySet().iterator().next(), newPartitions);
        adminClient.createPartitions(topic);
        System.out.println("topic #" + topicPartition.keySet().iterator().next() + " increased partition.");
    }


    @Override
    public void close() throws Exception {
        adminClient.close();
    }
}
